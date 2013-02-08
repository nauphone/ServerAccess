package ru.naumen.servacc.platform;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public class MacOsXTerminal extends Terminal
{
    @Override
    public void connect(int port, Map<String, String> params) throws IOException
    {
        new ProcessBuilder("open", MessageFormat.format("telnet://127.0.0.1:{0,number,#}", port)).start();
    }
}
