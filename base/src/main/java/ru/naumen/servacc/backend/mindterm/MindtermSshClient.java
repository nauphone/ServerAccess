package ru.naumen.servacc.backend.mindterm;

import com.mindbright.ssh2.SSH2SimpleClient;
import ru.naumen.servacc.backend.ISshClient;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public class MindtermSshClient implements ISshClient {
    private final SSH2SimpleClient client;

    public MindtermSshClient(SSH2SimpleClient client) {
        this.client = client;
    }

    @Override
    public SSH2SimpleClient unwrap(Class clazz) {
        return client;
    }

    @Override
    public boolean isConnected() {
        return client.getTransport().isConnected();
    }

    @Override
    public void normalDisconnect(String message) {
        client.getTransport().normalDisconnect(message);
    }
}
