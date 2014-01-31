package ru.naumen.servacc.platform;

import java.util.Iterator;

import ru.naumen.servacc.settings.ListProvider;

public final class OS
{
    private OS()
    {
        // Utility class should not have public constructor
    }

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
     *
     * @param settingsProvider
     * @param defaultCommand
     * @return
     */
    public static Command buildCommand(ListProvider settingsProvider, Command defaultCommand)
    {
        Iterator<String> iterator = settingsProvider.list().iterator();
        if (iterator.hasNext())
        {
            return new Command(iterator.next());
        }
        return defaultCommand;
    }

    private static boolean isMacOSX()
    {
        return "Mac OS X".equalsIgnoreCase(System.getProperty("os.name"));
    }

    private static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }
}
