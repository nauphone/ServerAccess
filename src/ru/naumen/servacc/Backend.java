/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
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
import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.swt.program.Program;

import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.telnet.ConsoleManager;

import com.mindbright.ssh2.SSH2SessionChannel;
import com.mindbright.ssh2.SSH2SimpleClient;

/**
 * @author tosha
 *
 */
public class Backend extends SSH2Backend
{
    private static Socket openTerminal(String options) throws Exception
    {
        ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST);
        try
        {
            server.setSoTimeout(SocketUtils.DEFAULT_TIMEOUT);
            Object[] params = new Object[]{SocketUtils.LOCALHOST, server.getLocalPort(), options};
            if (Util.isLinux())
            {
                Runtime.getRuntime().exec(MessageFormat.format("putty {2} -telnet {0} -P {1,number,#}", params));
            }
            else if (Util.isMacOSX())
            {
                Runtime.getRuntime().exec(MessageFormat.format("open telnet://{0}:{1,number,#}", params));
            }
            else
            {
                Runtime.getRuntime().exec(MessageFormat.format("cmd /C start putty {2} -telnet {0} -P {1,number,#}", params));
            }
            // FIXME: collect children and kill it on
            Socket socket = server.accept();
            return socket;
        }
        finally
        {
            server.close();
        }
    }

    private static Socket openFTPBrowser() throws Exception
    {
        ServerSocket server = SocketUtils.createListener(SocketUtils.LOCALHOST);
        try
        {
            server.setSoTimeout(SocketUtils.DEFAULT_TIMEOUT);
            Object[] params = new Object[]{SocketUtils.LOCALHOST, server.getLocalPort()};
            if (Util.isLinux())
            {
                Runtime.getRuntime().exec(MessageFormat.format("gftp ftp://anonymous@{0}:{1,number,#}", params));
            }
            else if (Util.isMacOSX())
            {
                Runtime.getRuntime().exec(MessageFormat.format("open ftp://anonymous@{0}:{1,number,#}", params));
            }
            else
            {
                Runtime.getRuntime().exec(MessageFormat.format("cmd /C explorer /n,ftp://{0}:{1,number,#}", params));
            }
            Socket socket = server.accept();
            return socket;
        }
        finally
        {
            server.close();
        }
    }

    private static void openURLInBrowser(String url) throws Exception
    {
        if (Util.isLinux())
        {
            Runtime.getRuntime().exec("firefox " + url);
        }
        else if (Util.isMacOSX())
        {
            // open URL in default browser
            Program.launch(url);
        }
        else
        {
            Runtime.getRuntime().exec("cmd /C start " + url);
        }
    }

    public void openSSHAccount(SSHAccount account) throws Exception
    {
        SSH2SimpleClient client = getSSH2Client(account);
        if (!client.getTransport().isConnected())
        {
            connections.remove(account.getUniqueIdentity());
            System.err.println("Connection broken, trying again");
            openSSHAccount(account);
            return;
        }
        SSH2SessionChannel session = client.getConnection().newSession();
        try
        {
            if (!session.requestPTY("xterm", 24, 80, new byte[] {12, 0, 0, 0, 0, 0}))
            {
                client.getTransport().normalDisconnect("bye bye");
                connections.remove(account.getUniqueIdentity());
                throw new IOException("Failed to get PTY on the remote server");
            }
            String puttyOptions = new String();
            if (account.params.containsKey("putty_options"))
            {
                puttyOptions = account.params.get("putty_options");
            }
            Socket term = openTerminal(puttyOptions);
            ConsoleManager console = new ConsoleManager(term, session, (HashMap) account.params);
            if (Util.isMacOSX())
            {
                // TODO: probably this should be done regardless of the TELNET
                // client being used, but this was not tested with PuTTY yet
                console.negotiateProtocolOptions();
            }
            session.changeStdIn(console.getInputStream());
            session.changeStdOut(console.getOutputStream());
            if (!session.doShell())
            {
                throw new IOException("Failed to start shell on the remote server");
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

    public void openHTTPAccount(HTTPAccount account) throws Exception
    {
        URL url = new URL(account.getURL());
        // Construct URL
        StringBuffer targetURL = new StringBuffer();
        // protocol
        targetURL.append(url.getProtocol() + "://");
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
            targetURL.append(userInfo + "@");
        }
        // host and port
        String host = url.getHost();
        int port = url.getPort() >= 0 ? url.getPort() : 80;
        SSHAccount throughAccount = getThrough(account);
        if (throughAccount != null)
        {
            //FIXME: host/port used twice as in/out params of this block
            int localPort = SocketUtils.getFreePort();
            localPortForward(throughAccount, localPort, host, port);
            host = SocketUtils.LOCALHOST;
            port = localPort;
        }
        targetURL.append(host + ":" + port);
        // path info
        targetURL.append(url.getPath());
        // query string
        if (url.getQuery() != null)
        {
            targetURL.append("?" + url.getQuery());
        }
        openURLInBrowser(targetURL.toString());
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
}
