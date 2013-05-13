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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.mindbright.ssh2.SSH2InternalChannel;
import org.apache.log4j.Logger;
import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author Andrey Hitrin
 * @since 27.12.12
 */
public class HTTPProxy
{
    private static final Logger LOG = Logger.getLogger(HTTPProxy.class);
    private final Backend backend;
    private final ExecutorService executor;

    private int port;
    private SSHAccount serverAccount;
    private Future<?> serverTask;
    private MessageListener listener;

    public HTTPProxy(Backend backend, ExecutorService executor)
    {
        this.backend = backend;
        this.executor = executor;
        this.listener = new MessageListener()
        {
            @Override
            public void notify(String text)
            {
            }
        };
    }

    public void setProxyOn(SSHAccount account, int localPort, MessageListener messageListener)
    {
        serverAccount = account;
        port = localPort;
        listener = messageListener;
        restartProxyServer();
    }

    private void restartProxyServer()
    {
        finish();
        start();
    }

    public void start()
    {
        serverTask = executor.submit(new Server(serverAccount, port));
    }

    public void finish()
    {
        if (!serverTask.isDone())
        {
            serverTask.cancel(true);
        }
    }

    public class Server implements Runnable
    {
        private final SSHAccount serverAccount;
        private final int port;

        public Server(SSHAccount serverAccount, int port)
        {
            this.serverAccount = serverAccount;
            this.port = port;
        }

        @Override
        public void run()
        {
            try
            {
                ServerSocket serverSocket = new ServerSocket(port);
                while (true)
                {
                    Socket s = serverSocket.accept();
                    executor.submit(new Listener(s, serverAccount));
                }
            }
            catch (IOException e)
            {
                LOG.error("Exception during main proxy loop", e);
                listener.notify(e.getMessage());
            }
        }
    }

    public class Listener implements Runnable
    {
        public static final int MAX_BUFFER_SIZE = 4096;
        private final Socket socket;
        private final SSHAccount account;
        private String request;
        private String host;
        private int port;
        private PushbackInputStream clientInputStream;

        public Listener(Socket s, SSHAccount account)
        {
            this.socket = s;
            this.account = account;
        }

        @Override
        public void run()
        {
            try
            {
                readSocket();
                if (request.length() > 0)
                {
                    parseRequest();
                    LOG.debug("Got request: " + request + " -> " + host + ":" + port);
                    SSH2InternalChannel channel = backend.openProxyConnection(host, port, account);
                    crossConnectStreams(channel, socket);
                }
            }
            catch (Exception e)
            {
                LOG.error("Error during listener loop", e);
            }
        }

        private void readSocket() throws IOException
        {
            clientInputStream = new PushbackInputStream(socket.getInputStream(), 1024);
            int symbol;
            StringBuilder builder = new StringBuilder();
            while ((symbol = clientInputStream.read()) != -1)
            {
                builder.append((char) symbol);
                if (symbol == '\n')
                {
                    break;
                }
            }
            clientInputStream.unread(builder.toString().getBytes());

            request = builder.toString();
        }

        private void parseRequest() throws MalformedURLException
        {
            final URL requestedURL = new URLBuilder(request).toURL();
            final String protocol = requestedURL.getProtocol();
            if(!supportedProtocol(protocol))
            {
                throw new MalformedURLException("Unsupported protocol detected: " + protocol);
            }
            host = requestedURL.getHost();
            port = requestedURL.getPort();
            if (port == -1)
            {
                port = 80;
            }
        }

        private boolean supportedProtocol(String protocol)
        {
            return "http".equals(protocol) || "".equals(protocol);
        }

        private void crossConnectStreams(SSH2InternalChannel channel, Socket socket) throws IOException
        {
            final OutputStream channelOutputStream = channel.getOutputStream();
            final InputStream channelInputStream = channel.getInputStream();
            final OutputStream clientOutputStream = socket.getOutputStream();

            connectStreams(clientInputStream, channelOutputStream);
            connectStreams(channelInputStream, clientOutputStream);
        }

        private void connectStreams(final InputStream inputStream, final OutputStream outputStream)
        {
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    byte[] buffer = new byte[MAX_BUFFER_SIZE];
                    int bufferSize;

                    try
                    {
                        while ((bufferSize = inputStream.read(buffer)) != -1)
                        {
                            outputStream.write(buffer, 0, bufferSize);
                            outputStream.flush();
                        }
                    }
                    catch (IOException e)
                    {
                        LOG.error("Failed to read connection " + inputStream.toString(), e);
                    }

                    try
                    {
                        LOG.debug("Closing stream: " + outputStream.toString());
                        outputStream.close();
                    }
                    catch (IOException e)
                    {
                        LOG.error("Failed to close connection " + outputStream.toString(), e);
                    }
                }
            });
        }
    }

    /**
     * @author Andrey Hitrin
     * @since 16.01.13
     */
    public static class URLBuilder
    {
        private String request;

        public URLBuilder(String request)
        {
            this.request = request;
        }

        public URL toURL() throws MalformedURLException
        {
            int nextAfterFirstSpace = request.indexOf(' ') + 1;
            int secondSpace = request.indexOf(' ', nextAfterFirstSpace);
            return new URL(request.substring(nextAfterFirstSpace, secondSpace));
        }
    }
}
