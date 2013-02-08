package ru.naumen.servacc.platform;

import java.io.IOException;
import java.util.Map;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public abstract class Terminal
{
    public abstract void connect(int localPort, Map<String, String> params) throws IOException;
}
