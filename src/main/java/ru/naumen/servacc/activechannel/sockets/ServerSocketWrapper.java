/**
 * 
 */
package ru.naumen.servacc.activechannel.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public abstract class ServerSocketWrapper implements AutoCloseable
{
    private ServerSocket serverSocket;
    private SSHAccount account;
    
    public ServerSocketWrapper(ServerSocket serverSocket, SSHAccount account)
    {
        this.serverSocket = serverSocket;
        this.account = account;
    }

    public Socket accept() throws IOException
    {
        Socket acceptedSocket = serverSocket.accept();
        
        register(account, serverSocket, acceptedSocket);
        
        return acceptedSocket;
    }
    
    protected abstract void register(SSHAccount account, ServerSocket serverSocket, Socket acceptedSocket);
    
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
