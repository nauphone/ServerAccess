package ru.naumen.servacc.platform;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public class MacOsXTerminal extends Terminal
{
    public MacOsXTerminal()
    {
        super("open  telnet://{host}:{port}");
    }
}
