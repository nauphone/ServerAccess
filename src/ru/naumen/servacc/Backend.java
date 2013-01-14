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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.platform.Platform;
import ru.naumen.servacc.telnet.ConsoleManager;
import ru.naumen.servacc.util.Util;

import com.mindbright.ssh2.SSH2SessionChannel;
import com.mindbright.ssh2.SSH2SimpleClient;
import org.apache.log4j.Logger;

/**
 * @author tosha
 */

public class Backend extends SSH2Backend
{
    private static final Logger LOGGER = Logger.getLogger(Backend.class);
    private final Platform platform;
    private final ExecutorService executor;

    public Backend(Platform platform, ExecutorService executorService)
    {
        this.platform = platform;
        this.executor = executorService;
    }

    public void openSSHAccount(final SSHAccount account) throws Exception
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
                        openSSHAccount(account, clientCopy);
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
        openSSHAccount(account, client);
    }

    private void openSSHAccount(final SSHAccount account, final SSH2SimpleClient client) throws Exception
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
            final Socket term = openTerminal(account);
            final ConsoleManager console = new ConsoleManager(term, session, account.getPassword(), account.needSudoLogin());
            if (platform.needToNegotiateProtocolOptions())
            {
                // TODO: probably this should be done regardless of the TELNET
                // client being used, but this was not tested with PuTTY yet
                console.negotiateProtocolOptions();
            }
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

    private Socket openTerminal(SSHAccount account) throws Exception
    {
        String options = "";
        if (account.getParams().containsKey("putty_options"))
        {
            options = account.getParams().get("putty_options");
        }
        ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST);
        try
        {
            server.setSoTimeout(SocketUtils.WARM_TIMEOUT);
            platform.openTerminal(new Object[] {SocketUtils.LOCALHOST, server.getLocalPort(), options});
            // FIXME: collect children and kill it on (on?)
            return server.accept();
        }
        finally
        {
            server.close();
        }
    }

    public void openHTTPAccount(HTTPAccount account) throws Exception
    {
        URL url = new URL(account.getURL());
        // Construct URL
        StringBuffer targetURL = new StringBuffer();
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
        final String host = url.getHost();
        final int port = url.getPort() >= 0 ? url.getPort() : 80;
        String targetHost;
        int targetPort;
        SSHAccount throughAccount = getThrough(account);
        if (throughAccount != null)
        {
            targetHost = SocketUtils.LOCALHOST;
            targetPort = SocketUtils.getFreePort();
            localPortForward(throughAccount, targetPort, host, port);
        }
        else
        {
            targetHost = host;
            targetPort = port;
        }
        targetURL.append(targetHost).append(':').append(targetPort);
        // path info
        targetURL.append(url.getPath());
        // query string
        if (url.getQuery() != null)
        {
            targetURL.append('?').append(url.getQuery());
        }
        platform.openInBrowser(targetURL.toString());
    }

    public void localPortForward(SSHAccount account, int localPort, String remoteHost, int remotePort) throws Exception
    {
        SSH2SimpleClient client = getSSH2Client(account);
        client.getConnection().newLocalForward(SocketUtils.LOCALHOST, localPort, remoteHost, remotePort);
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

    private Socket openFTPBrowser() throws Exception
    {
        ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST);
        try
        {
            server.setSoTimeout(SocketUtils.COLD_TIMEOUT);
            Object[] params = new Object[] {SocketUtils.LOCALHOST, server.getLocalPort()};
            platform.openFTPBrowser(params);
            return server.accept();
        }
        finally
        {
            server.close();
        }
    }
}
