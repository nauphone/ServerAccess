package ru.naumen.servacc;

import com.mindbright.ssh2.SSH2SimpleClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores list of connections.
 *
 * @author tosha
 *         Extracted @since 22.11.12
 */
public class ConnectionsManager
{
    private List<SSH2SimpleClient> connections;
    private Map<String, SSH2SimpleClient> cache;

    public ConnectionsManager()
    {
        cache = new ConcurrentHashMap<String, SSH2SimpleClient>();
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
