package ru.naumen.servacc.platform;

import java.util.Iterator;

import ru.naumen.servacc.settings.ListProvider;
import ru.naumen.servacc.settings.impl.DefaultConfiguration;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public class OS
{
    private final Platform platform;
    private final DefaultConfiguration configuration;

    public OS()
    {
        platform = detectPlatform();
        configuration = DefaultConfiguration.create(platform);
    }

    private Platform detectPlatform()
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

    private static boolean isMacOSX()
    {
        return "Mac OS X".equalsIgnoreCase(System.getProperty("os.name"));
    }

    private static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public Platform getPlatform()
    {
        return platform;
    }

    public DefaultConfiguration getConfiguration()
    {
        return configuration;
    }

    public Command getBrowser()
    {
        return buildCommand(configuration.filterProperties("browser"), platform.defaultBrowser());
    }

    public Command getFTPBrowser()
    {
        return buildCommand(configuration.filterProperties("ftp"), platform.defaultFTPBrowser());
    }

    public Command getTerminal()
    {
        return buildCommand(configuration.filterProperties("terminal"), platform.defaultTerminal());
    }

    private Command buildCommand(ListProvider settingsProvider, Command defaultCommand)
    {
        Iterator<String> iterator = settingsProvider.list().iterator();
        if (iterator.hasNext())
        {
            return new Command(iterator.next());
        }
        return defaultCommand;
    }
}
