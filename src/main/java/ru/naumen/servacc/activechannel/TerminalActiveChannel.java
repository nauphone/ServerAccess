/**
 * 
 */
package ru.naumen.servacc.activechannel;

import java.net.ServerSocket;
import java.net.Socket;

import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class TerminalActiveChannel extends SocketActiveChannel
{
    public TerminalActiveChannel(IActiveChannelThrough parent, Socket socket, ServerSocket server)
    {
        super(parent, socket, server);
    }
    
    @Override
    public String getId()
    {
        return String.valueOf(getSocket().getPort());
    }

    @Override
    public boolean matches(String filter)
    {
        return true;
    }

    @Override
    public String getIconName()
    {
        return "/icons/application-terminal.png";
    }

    @Override
    public String toString()
    {
        return "ports: " + getSocket().getPort() + " to " + getServer().getLocalPort() + " (terminal)";
    }
}
