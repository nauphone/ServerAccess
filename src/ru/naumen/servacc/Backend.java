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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.mindbright.ssh2.SSH2InternalChannel;
import com.mindbright.ssh2.SSH2SessionChannel;
import com.mindbright.ssh2.SSH2SimpleClient;
import org.apache.log4j.Logger;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.platform.Command;
import ru.naumen.servacc.telnet.ConsoleManager;
import ru.naumen.servacc.util.Util;

/**
 * @author tosha
 */

public class Backend extends SSH2Backend
{
    private static final Logger LOGGER = Logger.getLogger(Backend.class);
    private final Command browser;
    private final Command terminal;
    private final Command ftpBrowser;
    private final ExecutorService executor;

    public Backend(Command browser, Command ftpBrowser, Command terminal, ExecutorService executorService)
    {
        this.browser = browser;
        this.ftpBrowser = ftpBrowser;
        this.terminal = terminal;
        this.executor = executorService;
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

    private void openSSHAccount(final SSHAccount account, final SSH2SimpleClient client, final String path) throws Exception
    {
        final SSH2SessionChannel session = client.getConnection().newSession();
        try
        {
            if (!session.requestPTY("xterm", 24, 80, new byte[]{ 12, 0, 0, 0, 0, 0 }))
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
        ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST);
        Map<String, String> params = new HashMap<String, String>(account.getParams());
        params.put("name", path);
        try
        {
            server.setSoTimeout(SocketUtils.WARM_TIMEOUT);
            terminal.connect(server.getLocalPort(), params);
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
        browser.open(buildUrl(account));
    }

    private String buildUrl(HTTPAccount account) throws Exception
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
        final String remoteHost = url.getHost();
        final int remotePort = url.getPort() >= 0 ? url.getPort() : 80;
        String targetHost;
        int targetPort;
        SSHAccount throughAccount = getThrough(account);
        if (throughAccount != null)
        {
            targetHost = SocketUtils.LOCALHOST;
            targetPort = SocketUtils.getFreePort();
            localPortForward(throughAccount, targetPort, remoteHost, remotePort);
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

    private Socket openFTPBrowser() throws IOException
    {
        ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST);
        try
        {
            server.setSoTimeout(SocketUtils.COLD_TIMEOUT);
            ftpBrowser.connect(server.getLocalPort(), Collections.EMPTY_MAP);
            return server.accept();
        }
        finally
        {
            server.close();
        }
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
}
