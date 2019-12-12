package ru.naumen.servacc.backend.sshd;

import java.io.IOException;
import org.apache.sshd.client.session.ClientSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.naumen.servacc.backend.ISshClient;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public class SshdSshClient implements ISshClient {
    private static final int BY_APPLICATION = 11;
    private static final Logger LOG = LoggerFactory.getLogger(SshdSshClient.class);

    private final ClientSession client;

    public SshdSshClient(ClientSession client) {
        this.client = client;
    }

    @Override
    public ClientSession unwrap(Class clazz) {
        return client;
    }

    @Override
    public boolean isConnected() {
        return client.isOpen();
    }

    @Override
    public void normalDisconnect(String message) {
        try {
            client.disconnect(BY_APPLICATION, message);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
