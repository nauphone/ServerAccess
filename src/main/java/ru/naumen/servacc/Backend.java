/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mindbright.jca.security.SecureRandom;
import com.mindbright.ssh2.SSH2AuthKbdInteract;
import com.mindbright.ssh2.SSH2AuthPassword;
import com.mindbright.ssh2.SSH2Authenticator;
import com.mindbright.ssh2.SSH2InternalChannel;
import com.mindbright.ssh2.SSH2SessionChannel;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.util.RandomSeed;
import com.mindbright.util.SecureRandomAndPad;
import org.apache.log4j.Logger;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.Path;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.platform.Command;
import ru.naumen.servacc.platform.OS;
import ru.naumen.servacc.telnet.ConsoleManager;
import ru.naumen.servacc.util.Util;

/**
 * @author tosha
 */
public class Backend
{
    private static final int SSH_DEFAULT_PORT = 22;
    private static final Logger LOGGER = Logger.getLogger(Backend.class);
    private static RandomSeed seed;
    private static SecureRandomAndPad secureRandom;
    private final Command browser;
    private final Command terminal;
    private final Command ftpBrowser;
    private final ExecutorService executor;
    private final ConnectionsManager connections;
    private SSHAccount globalThrough;
    private GlobalThroughView globalThroughView;

    public Backend(OS system, ExecutorService executorService)
    {
        this.browser = system.getBrowser();
        this.ftpBrowser = system.getFTPBrowser();
        this.terminal = system.getTerminal();
        this.executor = executorService;
        connections = new ConnectionsManager();
    }

    public void openSSHAccount(final SSHAccount account, final String path) throws Exception
    {
        SSH2SimpleClient client;
        if (isConnected(account))
        {
            client = getSSH2Client(account);
            // this is to force timeout when reusing a cached connection
            // in order to detect if a connection is hung more quickly
            try
            {
                final SSH2SimpleClient clientCopy = client;
                Future<Object> f = this.executor.submit(new Callable<Object>()
                {
                    @Override
                    public Object call() throws Exception
                    {
                        openSSHAccount(account, clientCopy, path);
                        return null;
                    }
                });
                f.get(SocketUtils.WARM_TIMEOUT, TimeUnit.MILLISECONDS);
                return;
            }
            catch (TimeoutException e)
            {
                removeConnection(account);
                LOGGER.error("Connection is broken, retrying", e);
            }
        }
        // try with "cold" timeout
        client = getSSH2Client(account);
        openSSHAccount(account, client, path);
    }

    public void openHTTPAccount(HTTPAccount account) throws Exception
    {
        browser.open(buildUrl(account));
    }

    public void localPortForward(SSHAccount account, String localHost, int localPort, String remoteHost, int remotePort) throws Exception
    {
        SSH2SimpleClient client = getSSH2Client(account);
        client.getConnection().newLocalForward(localHost, localPort, remoteHost, remotePort);
    }

    public void browseViaFTP(SSHAccount account) throws Exception
    {
        SSH2SimpleClient client = getSSH2Client(account);
        Socket socket = openFTPBrowser();
        new FTP2SFTPProxy(
            client.getConnection(),
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
    public synchronized SSH2InternalChannel openProxyConnection(String host, int port, SSHAccount account) throws Exception
    {
        if (account != null)
        {
            SSH2SimpleClient client = getSSH2Client(account);
            return client.getConnection().newLocalInternalForward(host, port);
        }
        return null;
    }

    public SSHAccount getThrough(Account account)
    {
        SSHAccount throughAccount = null;
        if (account.getThrough() != null)
        {
            throughAccount = (SSHAccount) account.getThrough();
        }
        else if (globalThrough != null)
        {
            throughAccount = globalThrough;
        }

        return throughAccount;
    }

    public void cleanup()
    {
        connections.cleanup();
    }

    public void setGlobalThrough(SSHAccount account)
    {
        globalThrough = account;
        connections.clearCache();
    }

    public void setGlobalThroughView(GlobalThroughView view)
    {
        globalThroughView = view;
    }

    public void selectNewGlobalThrough(String uniqueIdentity, IConfig config)
    {
        Path path = Path.find(config, uniqueIdentity);
        if (path.found())
        {
            globalThroughView.setGlobalThroughWidget(path.path());
            setGlobalThrough(path.account());
        }
        else
        {
            clearGlobalThrough();
        }
    }

    public void refresh(IConfig newConfig)
    {
        String identity = "";
        if (globalThrough != null)
        {
            identity = globalThrough.getUniqueIdentity();
        }
        selectNewGlobalThrough(identity, newConfig);
    }

    public void clearGlobalThrough()
    {
        globalThroughView.clearGlobalThroughWidget();
        setGlobalThrough(null);
    }

    private static SecureRandomAndPad nextSecure()
    {
        if (seed == null)
        {
            seed = new RandomSeed();
        }
        if (secureRandom == null)
        {
            secureRandom = new SecureRandomAndPad(new SecureRandom(seed.getBytesBlocking(20, false)));
        }
        return secureRandom;
    }

    private void openSSHAccount(final SSHAccount account, final SSH2SimpleClient client, final String path) throws Exception
    {
        final SSH2SessionChannel session = client.getConnection().newSession();
        try
        {
            if (!session.requestPTY("xterm", 24, 80, new byte[] {12, 0, 0, 0, 0, 0}))
            {
                client.getTransport().normalDisconnect("bye bye");
                removeConnection(account);
                throw new IOException("Failed to get PTY on remote side");
            }
            final Socket term = openTerminal(account, path);
            final ConsoleManager console = new ConsoleManager(term, session, account.getPassword(), account.needSudoLogin());
            console.negotiateProtocolOptions();
            session.changeStdIn(console.getInputStream());
            session.changeStdOut(console.getOutputStream());
            if (!session.doShell())
            {
                throw new IOException("Failed to start shell on remote side");
            }
        }
        catch (Exception e)
        {
            if (session != null)
            {
                session.close();
            }
            throw e;
        }
    }

    private Socket openTerminal(SSHAccount account, String path) throws IOException
    {
        try (ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST))
        {
            Map<String, String> params = new HashMap<>(account.getParams());
            params.put("name", path);
            server.setSoTimeout(SocketUtils.WARM_TIMEOUT);
            terminal.connect(server.getLocalPort(), params);
            // FIXME: collect children and kill it on (on?)
            return server.accept();
        }
    }

    private String buildUrl(HTTPAccount account) throws Exception
    {
        URL url = new URL(account.getURL());
        // Construct URL
        StringBuilder targetURL = new StringBuilder();
        // protocol
        targetURL.append(url.getProtocol()).append("://");
        // user (authentication) info
        String userInfo;
        if (account.getLogin() != null)
        {
            String password = account.getPassword();
            password = password != null ? password : "";
            userInfo = account.getLogin() + ":" + password;
        }
        else
        {
            userInfo = url.getUserInfo();
        }
        if (!Util.isEmptyOrNull(userInfo))
        {
            targetURL.append(userInfo).append('@');
        }
        // host and port
        final String remoteHost = url.getHost();
        final int remotePort = url.getPort() >= 0 ? url.getPort() : 80;
        String targetHost;
        int targetPort;
        SSHAccount throughAccount = getThrough(account);
        if (throughAccount != null)
        {
            targetHost = SocketUtils.LOCALHOST;
            targetPort = SocketUtils.getFreePort();
            localPortForward(throughAccount, SocketUtils.LOCALHOST, targetPort, remoteHost, remotePort);
        }
        else
        {
            targetHost = remoteHost;
            targetPort = remotePort;
        }
        targetURL.append(targetHost).append(':').append(targetPort);
        // path info
        targetURL.append(url.getPath());
        // query string
        if (url.getQuery() != null)
        {
            targetURL.append('?').append(url.getQuery());
        }
        return targetURL.toString();
    }

    private Socket openFTPBrowser() throws IOException
    {
        try (ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST))
        {
            server.setSoTimeout(SocketUtils.COLD_TIMEOUT);
            ftpBrowser.connect(server.getLocalPort(), Collections.<String, String>emptyMap());
            return server.accept();
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
    private SSH2SimpleClient getSSH2Client(SSHAccount account) throws Exception
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
        SSH2SimpleClient last = null;
        for (SSHAccount through : throughChain)
        {
            if (isConnected(through))
            {
                last = getConnection(through);
            }
            else
            {
                last = getSSH2Client(through, last);
                saveConnection(through, last);
            }
        }
        SSH2SimpleClient client = getSSH2Client(account, last);
        saveConnection(account, client);
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
    private SSH2SimpleClient getSSH2Client(SSHAccount account, SSH2SimpleClient through) throws Exception
    {
        String host = account.getHost();
        int port = account.getPort() >= 0 ? account.getPort() : SSH_DEFAULT_PORT;
        if (through != null)
        {
            int localPort = SocketUtils.getFreePort();
            //FIXME: localize newLocalForward usage in localPortForward
            through.getConnection().newLocalForward(SocketUtils.LOCALHOST, localPort, host, port);
            host = SocketUtils.LOCALHOST;
            port = localPort;
        }
        return createSSH2Client(host, port, account);
    }

    private SSH2SimpleClient createSSH2Client(String host, Integer port, final SSHAccount account) throws Exception
    {
        SecureRandomAndPad secureRandomAndPad = Backend.nextSecure();
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress(host, port), SocketUtils.COLD_TIMEOUT);
        SSH2Transport transport = new SSH2Transport(sock, secureRandomAndPad)
        {
            @Override
            protected void disconnectInternal(int i, String s, String s1, boolean b)
            {
                super.disconnectInternal(i, s, s1, b);
                removeConnection(account);
            }
        };
        SSH2Authenticator auth = new SSH2Authenticator(account.getLogin());
        auth.addModule(new SSH2AuthPassword(account.getPassword()));
        auth.addModule(new SSH2AuthKbdInteract(new SSH2PasswordInteractor(account.getPassword())));
        return new SSH2SimpleClient(transport, auth);
    }

    private void saveConnection(SSHAccount account, SSH2SimpleClient client)
    {
        connections.put(account.getUniqueIdentity(), client);
    }

    private SSH2SimpleClient getConnection(SSHAccount account)
    {
        return connections.get(account.getUniqueIdentity());
    }

    private boolean isConnected(SSHAccount account)
    {
        return connections.containsKey(account.getUniqueIdentity());
    }

    private void removeConnection(SSHAccount account)
    {
        connections.remove(account.getUniqueIdentity());
    }
}
