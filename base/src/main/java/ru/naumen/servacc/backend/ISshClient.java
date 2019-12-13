package ru.naumen.servacc.backend;

/**
 * @author Arkaev Andrei
 * @since 13.12.2019
 */
public interface ISshClient {
    <T> T unwrap(Class clazz);

    boolean isConnected();
    void normalDisconnect(String message);
}
