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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mindbright.jca.security.SecureRandom;
import com.mindbright.ssh2.SSH2AuthKbdInteract;
import com.mindbright.ssh2.SSH2AuthPassword;
import com.mindbright.ssh2.SSH2Authenticator;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.util.RandomSeed;
import com.mindbright.util.SecureRandomAndPad;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.Path;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;

public class SSH2Backend
{
    public static final int SSH_DEFAULT_PORT = 22;

    private ConnectionsManager connections;
    private SSHAccount globalThrough;
    private static RandomSeed seed;
    private static SecureRandomAndPad secureRandom;
    private GlobalThroughView globalThroughView;

    private static SecureRandomAndPad nextSecure()
    {
        if (seed == null)
        {
            seed = new RandomSeed();
        }
        if (secureRandom == null)
        {
            secureRandom = new SecureRandomAndPad(new SecureRandom(seed.getBytesBlocking(20, false)));
        }
        return secureRandom;
    }

    public SSH2Backend()
    {
        super();
        connections = new ConnectionsManager();
    }

    /**
     * Retrieve SSH2 connection described by account (follow "through chain").
     * Complex function, creating 0...n SSH2 connection.
     *
     * @param account
     * @return
     * @throws Exception
     */
    protected SSH2SimpleClient getSSH2Client(SSHAccount account) throws Exception
    {
        if (isConnected(account))
        {
            return getConnection(account);
        }
        // follow the 'through' chain
        List<SSHAccount> throughChain = new ArrayList<SSHAccount>();
        SSHAccount cur = getThrough(account);
        while (cur != null)
        {
            if (throughChain.contains(cur))
            {
                // circular reference
                break;
            }
            throughChain.add(cur);
            if (isConnected(cur))
            {
                // account is found in the cache, no need to go further
                break;
            }
            cur = getThrough(cur);
        }
        Collections.reverse(throughChain);
        SSH2SimpleClient last = null;
        for (SSHAccount through : throughChain)
        {
            if (isConnected(through))
            {
                last = getConnection(through);
            }
            else
            {
                last = getSSH2Client(through, last);
                saveConnection(through, last);
            }
        }
        SSH2SimpleClient client = getSSH2Client(account, last);
        saveConnection(account, client);
        return client;
    }

    /**
     * Retrieve SSH2 connection described by account using through connection (if not null) as tunnel.
     * Simple function creating exactly 1 (one) SSH2 connection.
     * Do not use this function - it is only extension of another getSSH2Client.
     *
     * @param account
     * @param through
     * @return
     * @throws Exception
     */
    private SSH2SimpleClient getSSH2Client(SSHAccount account, SSH2SimpleClient through) throws Exception
    {
        String host = account.getHost();
        int port = account.getPort() >= 0 ? account.getPort() : SSH_DEFAULT_PORT;
        if (through != null)
        {
            int localPort = SocketUtils.getFreePort();
            through.getConnection().newLocalForward(SocketUtils.LOCALHOST, localPort, host, port); //FIXME: localize newLocalForward usage in localPortForward
            host = SocketUtils.LOCALHOST;
            port = localPort;
        }
        return createSSH2Client(host, port, account);
    }

    private SSH2SimpleClient createSSH2Client(String host, Integer port, final SSHAccount account) throws Exception
    {
        SecureRandomAndPad secureRandomAndPad = nextSecure();
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress(host, port), SocketUtils.COLD_TIMEOUT);
        SSH2Transport transport = new SSH2Transport(sock, secureRandomAndPad)
        {
            @Override
            protected void disconnectInternal(int i, String s, String s1, boolean b)
            {
                super.disconnectInternal(i, s, s1, b);
                removeConnection(account);
            }
        };
        SSH2Authenticator auth = new SSH2Authenticator(account.getLogin());
        auth.addModule(new SSH2AuthPassword(account.getPassword()));
        auth.addModule(new SSH2AuthKbdInteract(new SSH2PasswordInteractor(account.getPassword())));
        return new SSH2SimpleClient(transport, auth);
    }

    public SSHAccount getThrough(Account account)
    {
        SSHAccount throughAccount = null;
        if (account.getThrough() != null)
        {
            throughAccount = (SSHAccount) account.getThrough();
        }
        else if (globalThrough != null)
        {
            throughAccount = globalThrough;
        }

        return throughAccount;
    }

    private void saveConnection(SSHAccount account, SSH2SimpleClient client)
    {
        connections.put(account.getUniqueIdentity(), client);
    }

    private SSH2SimpleClient getConnection(SSHAccount account)
    {
        return connections.get(account.getUniqueIdentity());
    }

    protected boolean isConnected(SSHAccount account)
    {
        return connections.containsKey(account.getUniqueIdentity());
    }

    protected void removeConnection(SSHAccount account)
    {
        connections.remove(account.getUniqueIdentity());
    }

    public void cleanup()
    {
        connections.cleanup();
    }

    public void setGlobalThrough(SSHAccount account)
    {
        globalThrough = account;
        connections.clearCache();
    }

    public void setGlobalThroughView(GlobalThroughView view)
    {
        globalThroughView = view;
    }

    public void selectNewGlobalThrough(String uniqueIdentity, IConfig config)
    {
        Path path = Path.find(config, uniqueIdentity);
        if (path.found())
        {
            globalThroughView.setGlobalThroughWidget(path.path());
            setGlobalThrough(path.account());
        }
        else
        {
            clearGlobalThrough();
        }
    }

    public void refresh(IConfig newConfig)
    {
        selectNewGlobalThrough(globalThrough.getUniqueIdentity(), newConfig);
    }

    public void clearGlobalThrough()
    {
        globalThroughView.clearGlobalThroughWidget();
        setGlobalThrough(null);
    }
}
