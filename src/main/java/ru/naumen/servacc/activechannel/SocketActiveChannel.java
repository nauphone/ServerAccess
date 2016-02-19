/**
 * 
 */
package ru.naumen.servacc.activechannel;

import java.net.ServerSocket;
import java.net.Socket;

import ru.naumen.servacc.SocketUtils;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public abstract class SocketActiveChannel extends ActiveChannel
{
    private Socket socket;
    private ServerSocket server;
    
    public SocketActiveChannel(IActiveChannelThrough parent, Socket socket, ServerSocket server)
    {
        super(parent);
        
        this.socket = socket;
        this.server = server;
    }
    
    public SocketActiveChannel(IActiveChannelThrough parent, Socket socket)
    {
        this(parent, socket, null);
    }
    
    public SocketActiveChannel(IActiveChannelThrough parent, ServerSocket server)
    {
        this(parent, null, server);
    }

    @Override
    public void close()
    {
        super.close();
        
        /* TODO: Use it
         * 
         * if (!socket.isClosed())
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                // Nothing to do
            }
        }
        
        if (!server.isClosed())
        {
            try
            {
                server.close();
            }
            catch (IOException e)
            {
                // Nothing to do
            }
        }*/
    }
    
    protected Socket getSocket()
    {
        return socket;
    }
    
    protected ServerSocket getServer()
    {
        return server;
    }

    @Override
    public boolean isActive()
    {
        if (socket != null && !SocketUtils.isPortFree(socket.getPort()))
        {
            return true;
        }
        
        if (server != null && !SocketUtils.isPortFree(server.getLocalPort()))
        {
            return true;
        }
        
        return false;
    }
}
