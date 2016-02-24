/**
 * 
 */
package ru.naumen.servacc.activechannel;

import com.mindbright.ssh2.SSH2SimpleClient;

import ru.naumen.servacc.ConnectionsManager;
import ru.naumen.servacc.SocketUtils;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.activechannel.i.IHidableChannel;
import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public class SSHActiveChannel extends ActiveChannelThrough implements IHidableChannel
{
    private SSHAccount sshAccount;
    
    private int port;
    private int portThrough = -1;
    
    private ConnectionsManager connManager;
    
    private boolean hidden;
    
    public SSHActiveChannel(IActiveChannelThrough parent, ActiveChannelsRegistry registry, SSHAccount sshAccount, int port, ConnectionsManager connManager)
    {
        super(parent, registry);
        
        this.sshAccount = sshAccount;
        this.port = port;
        this.connManager = connManager;
    }
    
    public SSHActiveChannel(IActiveChannelThrough parent, ActiveChannelsRegistry registry, SSHAccount sshAccount, int port, int portThrough, ConnectionsManager connManager)
    {
        this(parent, registry, sshAccount, port, connManager);
        
        this.portThrough = portThrough;
    }

    @Override
    public String getId()
    {
        return sshAccount.getSignature();
    }

    @Override
    public boolean isHidden()
    {
        return hidden;
    }
    
    @Override
    public void hide()
    {
        hidden = true;
    }

    @Override
    public boolean matches(String filter)
    {
        return true;
    }

    @Override
    public String getIconName()
    {
        return "/icons/ssh-channel.png";
    }

    @Override
    public void close()
    {
        super.close();
        
        // TODO: Close SSH client
    }

    @Override
    public String toString()
    {
        return "ports: " + port + (portThrough != -1 ? " via " + portThrough : "") + "(" + sshAccount.toString() + ")";
    }
    
    public SSH2SimpleClient getSSHClient()
    {
        String connectionId = getSSHAccountId();
        
        if (connManager.containsKey(connectionId))
        {
            return connManager.get(connectionId);
        }
        
        return null;
    }
    
    public String getSSHAccountId()
    {
        return sshAccount.getUniqueIdentity();
    }

    @Override
    public boolean isActive()
    {
        return !SocketUtils.isPortFree(port);
    }
}
