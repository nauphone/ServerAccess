/**
 * 
 */
package ru.naumen.servacc.activechannel.sockets;

import java.net.ServerSocket;
import java.net.Socket;

import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.activechannel.TerminalActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author vtarasov
 * @since 
 */
public class TerminalSocketWrapper extends ServerSocketWrapper
{
    public TerminalSocketWrapper(ServerSocket serverSocket, SSHAccount account)
    {
        super(serverSocket, account);
    }

    @Override
    protected void register(SSHAccount account, ServerSocket serverSocket, Socket acceptedSocket)
    {
        IActiveChannelThrough channel = ActiveChannelsRegistry.getInstance().findChannelThrough(account.getUniquePathReversed());
        
        if (channel != null)
        {
            new TerminalActiveChannel(channel, acceptedSocket, serverSocket).save();
        }
    }
}
