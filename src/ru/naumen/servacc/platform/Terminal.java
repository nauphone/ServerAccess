package ru.naumen.servacc.platform;

import java.io.IOException;
import java.util.Map;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public class Terminal
{
    protected final CommandBuilder builder;

    public Terminal(String command)
    {
        builder = new CommandBuilder(command);
    }

    public void connect(int localPort, Map<String, String> params) throws IOException
    {
        new ProcessBuilder(builder.build(localPort, params)).start();
    }
}
