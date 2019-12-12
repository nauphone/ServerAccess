/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.backend.sshd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.PtyChannelConfiguration;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.naumen.servacc.IAuthenticationParametersGetter;
import ru.naumen.servacc.SocketUtils;
import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.backend.AbstractBackend;
import ru.naumen.servacc.backend.DualChannel;
import ru.naumen.servacc.backend.ISshClient;
import ru.naumen.servacc.backend.mindterm.MindtermFTP2SFTPProxy;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.platform.OS;
import ru.naumen.servacc.telnet.ConsoleManager;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public class SshdBackend extends AbstractBackend {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshdBackend.class);

    private final SshdKeyLoader keyLoader;

    public SshdBackend(OS system, ExecutorService executorService, ActiveChannelsRegistry acRegistry,
        IAuthenticationParametersGetter authParamsGetter)
    {
        super(system, executorService, acRegistry);

        keyLoader = new SshdKeyLoader(authParamsGetter);
    }

    @Override
    public void openSSHAccount(final SSHAccount account, final String path) throws Exception
    {
        ClientSession client;
        if (isConnected(account))
        {
            client = getSSH2Client(account);
            // this is to force timeout when reusing a cached connection
            // in order to detect if a connection is hung more quickly
            try
            {
                final ClientSession clientCopy = client;
                Future<Object> f = this.executor.submit(() ->
                {
                    openSSHAccount(account, clientCopy, path);
                    return null;
                });
                f.get(SocketUtils.WARM_TIMEOUT, TimeUnit.MILLISECONDS);
                return;
            }
            catch (TimeoutException e)
            {
                removeSSHActiveChannel(account);
                removeConnection(account);
                LOGGER.error("Connection is broken, retrying", e);
            }
        }
        // try with "cold" timeout
        client = getSSH2Client(account);
        openSSHAccount(account, client, path);
    }

    @Override
    public void localPortForward(SSHAccount account, String localHost, int localPort, String remoteHost, int remotePort) throws Exception
    {
        ClientSession session = getSSH2Client(account);
        SshdSocketAddress bound = startLocalPortForwardingInt(session, localHost, localPort, remoteHost, remotePort);
        createSSHLocalForwardActiveChannel(account, bound.getPort());
    }

    @Override
    public void browseViaFTP(SSHAccount account) throws Exception
    {
        ClientSession session = getSSH2Client(account);
        Socket socket = openFTPBrowser(account);
        new MindtermFTP2SFTPProxy(
            session,
            socket.getInputStream(),
            socket.getOutputStream(),
            "FTP Server");
    }

    /**
     * Use ssh chain to tunnel HTTP traffic
     * TODO use better synchronized access
     *
     * @param host
     * @param port
     * @param account
     */
    @Override
    public synchronized DualChannel openProxyConnection(String host, int port, SSHAccount account) throws Exception
    {
        if (account != null)
        {
            ClientSession session = getSSH2Client(account);
            SshdSocketAddress bound = startLocalPortForwardingInt(session, host, port);
            return new SshdChannel(bound);
        }
        return null;
    }

    private void openSSHAccount(final SSHAccount account, final ClientSession client, final String path) throws IOException {

        Map<String, ?> env = new HashMap<>();
        PtyChannelConfiguration ptyConfig = new PtyChannelConfiguration();
        ptyConfig.setPtyType("xterm");
        ptyConfig.setPtyColumns(80);
        ptyConfig.setPtyColumns(24);

        final ChannelShell shell = client.createShellChannel(ptyConfig, env);
        shell.addCloseFutureListener((cf) -> {
            removeSSHActiveChannel(account);
            removeConnection(account);
        });

        try
        {
            final Socket term = openTerminal(account, path);
            final ConsoleManager console = new ConsoleManager(term, new SshdShell(shell), account.getPassword(), account.needSudoLogin());
            console.negotiateProtocolOptions();
            shell.setIn(console.getInputStream());
            shell.setOut(console.getOutputStream());

            try {
                shell.open().verify(SocketUtils.COLD_TIMEOUT);
            } catch (IOException e) {
                throw new IOException("Failed to start shell on remote side", e);
            }
        }
        catch (IOException e) {
            LOGGER.error("Failed to open SSH account", e);
            shell.close();
        }
    }

    /**
     * Retrieve SSH2 connection described by account (follow "through chain").
     * Complex function, creating 0...n SSH2 connection.
     *
     * @param account
     * @return
     * @throws Exception
     */
    private ClientSession getSSH2Client(SSHAccount account) throws Exception
    {
        if (isConnected(account))
        {
            return getConnection(account);
        }
        // follow the 'through' chain
        List<SSHAccount> throughChain = new ArrayList<>();
        SSHAccount cur = getThrough(account);
        while (cur != null)
        {
            if (throughChain.contains(cur))
            {
                // circular reference
                break;
            }
            throughChain.add(cur);
            if (isConnected(cur))
            {
                // account is found in the cache, no need to go further
                break;
            }
            cur = getThrough(cur);
        }
        Collections.reverse(throughChain);
        ClientSession last = null;
        for (SSHAccount through : throughChain)
        {
            if (isConnected(through))
            {
                last = getConnection(through);
            }
            else
            {
                last = getSSH2Client(through, last);
                saveConnection(through, new SshdSshClient(last));
            }
        }
        ClientSession client = getSSH2Client(account, last);
        saveConnection(account, new SshdSshClient(client));
        return client;
    }

    /**
     * Retrieve SSH2 connection described by account using through connection (if not null) as tunnel.
     * Simple function creating exactly 1 (one) SSH2 connection.
     * Do not use this function - it is only extension of another getSSH2Client.
     *
     * @param account
     * @param through
     * @return
     * @throws Exception
     */
    private ClientSession getSSH2Client(SSHAccount account, ClientSession through) throws Exception
    {
        String host = account.getHost();
        int port = account.getPort() >= 0 ? account.getPort() : SSH_DEFAULT_PORT;
        if (through != null)
        {
            int localPort = SocketUtils.getFreePort();
            SshdSocketAddress bound = startLocalPortForwardingInt(through, SocketUtils.LOCALHOST, localPort, host, port);
            return createSSH2Client(SocketUtils.LOCALHOST, bound.getPort(), true, account);
        }
        return createSSH2Client(host, port, false, account);
    }

    private ClientSession createSSH2Client(String host, Integer port, boolean through, final SSHAccount account) throws Exception
    {
        SshClient client = SshClient.setUpDefaultClient();
        client.addCloseFutureListener((cf) -> {
            removeSSHActiveChannel(account);
            removeConnection(account);
        });
        client.start();

        ClientSession session = client.connect(account.getLogin(), host, port)
            .verify(SocketUtils.COLD_TIMEOUT)
            .getSession();

        Socket sock = new Socket();
        sock.connect(new InetSocketAddress(host, port), SocketUtils.COLD_TIMEOUT);
        if (account.getPassword() != null)
        {
            session.addPasswordIdentity(account.getPassword());
        }
        else
        {
            KeyPair keyPair = keyLoader.getKeyPair(account.getParams());
            session.addPublicKeyIdentity(keyPair);
        }
        session.auth().verify(SocketUtils.COLD_TIMEOUT);

        createSSHActiveChannel(account, sock.getLocalPort(), through ? port : -1);
        return session;
    }

    private ClientSession getConnection(SSHAccount account)
    {
        ISshClient client = connections.get(account.getUniqueIdentity());
        return client == null ? null : client.unwrap(ClientSession.class);
    }

    private SshdSocketAddress startLocalPortForwardingInt(ClientSession session, String remoteHost, int remotePort) throws IOException {
        return this.startLocalPortForwardingInt(session, "", 0, remoteHost, remotePort);
    }


    private SshdSocketAddress startLocalPortForwardingInt(ClientSession session, String localHost, int localPort, String remoteHost, int remotePort)
        throws IOException {
        SshdSocketAddress local = new SshdSocketAddress(localHost, localPort);
        SshdSocketAddress remote = new SshdSocketAddress(remoteHost, remotePort);
        return session.startLocalPortForwarding(local, remote);
    }
}
