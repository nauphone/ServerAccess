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
import java.util.HashMap;
import java.util.List;

import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.SSHAccount;

import com.mindbright.jca.security.SecureRandom;
import com.mindbright.ssh2.SSH2AuthKbdInteract;
import com.mindbright.ssh2.SSH2AuthPassword;
import com.mindbright.ssh2.SSH2Authenticator;
import com.mindbright.ssh2.SSH2SimpleClient;
import com.mindbright.ssh2.SSH2Transport;
import com.mindbright.util.RandomSeed;
import com.mindbright.util.SecureRandomAndPad;

public class SSH2Backend
{
    protected class ConnectionsManager
    {
        private List<SSH2SimpleClient> connections;
        private HashMap<String, SSH2SimpleClient> cache;

        public ConnectionsManager()
        {
            cache = new HashMap<String, SSH2SimpleClient>();
            connections = new ArrayList<SSH2SimpleClient>();
        }

        public void put(String key, SSH2SimpleClient client)
        {
            cache.put(key, client);
        }

        public void remove(String key)
        {
            cache.remove(key);
        }

        public SSH2SimpleClient get(String key)
        {
            return cache.get(key);
        }

        public boolean containsKey(String key)
        {
            return cache.containsKey(key);
        }

        public void clearCache()
        {
            // keep track of all open connections so we can close them on exit
            connections.addAll(cache.values());
            cache.clear();
        }

        public void cleanup()
        {
            clearCache();
            for (SSH2SimpleClient client : connections)
            {
                if (client.getTransport().isConnected())
                {
                    client.getTransport().normalDisconnect("quit");
                }
            }
        }
    }

    protected ConnectionsManager connections;
    protected SSHAccount globalThrough;
    private static RandomSeed seed;
    private static SecureRandomAndPad secureRandom;

    private static SSH2SimpleClient createSSH2Client(String host, Integer port, String login, String password) throws Exception
    {
        if (seed == null)
        {
            seed = new RandomSeed();
        }
        if (secureRandom == null)
        {
            secureRandom = new SecureRandomAndPad(new SecureRandom(seed.getBytesBlocking(20, false)));
        }
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress(host, port), SocketUtils.COLD_TIMEOUT);
        SSH2Transport transport = new SSH2Transport(sock, secureRandom);
        SSH2Authenticator auth = new SSH2Authenticator(login);
        auth.addModule(new SSH2AuthPassword(password));
        auth.addModule(new SSH2AuthKbdInteract(new SSH2PasswordInteractor(password)));
        SSH2SimpleClient client = new SSH2SimpleClient(transport, auth);
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
    private static SSH2SimpleClient getSSH2Client(SSHAccount account, SSH2SimpleClient through) throws Exception
    {
        String host = account.getHost();
        int port = account.getPort() >= 0 ? account.getPort() : 22;
        if (through != null)
        {
            int localPort = SocketUtils.getFreePort();
            through.getConnection().newLocalForward(SocketUtils.LOCALHOST, localPort, host, port); //FIXME: localize newLocalForward usage in localPortForward
            host = SocketUtils.LOCALHOST;
            port = localPort;
        }
        String login = account.getLogin();
        String password = account.getPassword();
        SSH2SimpleClient client = createSSH2Client(host, port, login, password);
        return client;
    }

    protected SSH2Backend()
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
        String id = account.getUniqueIdentity();
        if (connections.containsKey(id))
        {
            return connections.get(id);
        }
        // follow the 'through' chain
        List<SSHAccount> throughChain = new ArrayList<SSHAccount>();
        SSHAccount cur = getThrough(account);
        while (cur instanceof SSHAccount)
        {
            if (throughChain.contains(cur))
            {
                // circular reference
                break;
            }
            throughChain.add(cur);
            if (connections.containsKey(cur.getUniqueIdentity()))
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
            String throughId = through.getUniqueIdentity();
            if (connections.containsKey(throughId))
            {
                last = connections.get(throughId);
            }
            else
            {
                last = getSSH2Client(through, last);
                connections.put(throughId, last);
            }
        }
        SSH2SimpleClient client = getSSH2Client(account, last);
        connections.put(id, client);
        return client;
    }

    protected SSHAccount getThrough(Account account)
    {
        SSHAccount throughAccount = null;
        if (account.through instanceof SSHAccount)
        {
            throughAccount = (SSHAccount) account.through;
        }
        else if (globalThrough instanceof SSHAccount)
        {
            throughAccount = globalThrough;
        }
        return throughAccount;
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

}
