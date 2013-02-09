package ru.naumen.servacc.platform;

import java.util.Iterator;

import ru.naumen.servacc.settings.ListProvider;

public class OS
{
    public static Platform platform()
    {
        if (isMacOSX())
        {
            return new MacOsX();
        }
        else if (isWindows())
        {
            return new Windows();
        }
        return new Linux();
    }

    /**
     * In Java 8, this method should be moved directly to the Platform interface
     * @param platform
     * @param terminalProvider
     * @return
     */
    public static Terminal terminal(Platform platform, ListProvider terminalProvider)
    {
        Iterator<String> iterator = terminalProvider.list().iterator();
        if (iterator.hasNext())
        {
            return new Terminal(iterator.next());
        }
        return platform.defaultTerminal();
    }

    public static boolean isMacOSX()
    {
        return "Mac OS X".equalsIgnoreCase(System.getProperty("os.name"));
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
