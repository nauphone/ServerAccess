package ru.naumen.servacc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ru.naumen.servacc.backend.ISshClient;

/**
 * Stores list of connections.
 *
 * @author tosha
 *         Extracted @since 22.11.12
 */
public class ConnectionsManager
{
    private List<ISshClient> connections;
    private Map<String, ISshClient> cache;

    public ConnectionsManager()
    {
        cache = new ConcurrentHashMap<>();
        connections = new ArrayList<>();
    }

    public void put(String key, ISshClient client)
    {
        cache.put(key, client);
    }

    public void remove(String key)
    {
        // TODO: We do not put connection into connections list, so it will not be closed at exit. Bug or feature?
        cache.remove(key);
    }

    public ISshClient get(String key)
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
        for (ISshClient client : connections)
        {
            if (client.isConnected())
            {
                client.normalDisconnect("quit");
            }
        }
    }
}
