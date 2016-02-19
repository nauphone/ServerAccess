/**
 * 
 */
package ru.naumen.servacc.activechannel;

import java.io.IOException;
import java.net.ServerSocket;

import ru.naumen.servacc.SocketUtils;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class SSHLocalForwardActiveChannel extends ActiveChannel
{
    private int port;
    
    public SSHLocalForwardActiveChannel(SSHActiveChannel parent, int port)
    {
        super(parent);
        
        this.port = port;
    }
    
    @Override
    public String getId()
    {
        return String.valueOf(port);
    }
    
    @Override
    public boolean matches(String filter)
    {
        return true;
    }
    
    @Override
    public String getIconName()
    {
        return null;
    }

    @Override
    public void close()
    {
        super.close();
        
        /* TODO: Use it
         * 
         * SSHActiveChannel parent = narrowParent();
        
        if (parent != null)
        {
            SSH2SimpleClient sshClient = parent.getSSHClient();
            
            if (sshClient != null && sshClient.getTransport().isConnected())
            {
                sshClient.getTransport().normalDisconnect("quit");
            }
        }*/
    }

    @Override
    public String toString()
    {
        return "port: " + port;
    }
    
    private SSHActiveChannel narrowParent()
    {
        return (SSHActiveChannel)getParent();
    }

    @Override
    public boolean isActive()
    {
        return !SocketUtils.isPortFree(port);
    }
}
