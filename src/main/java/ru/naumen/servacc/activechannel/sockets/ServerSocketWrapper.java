/**
 *
 */
package ru.naumen.servacc.activechannel.sockets;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;

import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class ServerSocketWrapper implements AutoCloseable
{
    private ServerSocket serverSocket;
    private SSHAccount account;
    private Class<? extends IActiveChannel> activeChannelClass;
    private ActiveChannelsRegistry registry;

    public ServerSocketWrapper(ServerSocket serverSocket, SSHAccount account, Class<? extends IActiveChannel> activeChannelClass, ActiveChannelsRegistry registry)
    {
        this.serverSocket = serverSocket;
        this.account = account;
        this.activeChannelClass = activeChannelClass;
        this.registry = registry;
    }

    public Socket accept() throws IOException
    {
        Socket acceptedSocket = serverSocket.accept();

        register(account, serverSocket, acceptedSocket);

        return acceptedSocket;
    }

    protected void register(SSHAccount account, ServerSocket serverSocket, Socket acceptedSocket)
    {
        if (account != null)
        {
            IActiveChannelThrough channel = registry.findChannelThrough(account.getUniquePathReversed());

            if (channel != null)
            {
                try
                {
                    Constructor<? extends IActiveChannel> activeChannelConstructor = activeChannelClass.getConstructor(
                            IActiveChannelThrough.class, ActiveChannelsRegistry.class, Socket.class, ServerSocket.class);
                    activeChannelConstructor.newInstance(channel, registry, acceptedSocket, serverSocket).save();
                }
                catch (Exception e)
                {
                    // Nothing to do because of programmers exception
                }
            }
        }
    }

    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    @Override
    public void close() throws IOException
    {
        serverSocket.close();
    }
}
