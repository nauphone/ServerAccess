package ru.naumen.servacc.platform;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public class Command
{
    private static final Logger LOGGER = Logger.getLogger(Command.class);
    private final CommandBuilder builder;

    public Command(String command)
    {
        builder = new CommandBuilder(command);
    }

    public void connect(int localPort, Map<String, String> params) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder(builder.build(localPort, params));
        printDebugInfo(processBuilder);
        processBuilder.start();
    }

    public void open(String url) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder(builder.build(url));
        printDebugInfo(processBuilder);
        processBuilder.start();
    }

    private void printDebugInfo(ProcessBuilder processBuilder)
    {
        List<String> command = processBuilder.command();
        StringBuilder stringBuilder = new StringBuilder("Run command sequence: [ ");
        for (String s : command)
        {
            stringBuilder.append("\"").append(s).append("\", ");
        }
        stringBuilder.append("]");
        LOGGER.debug(stringBuilder.toString());
    }
}
