package ru.naumen.servacc.platform;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import ru.naumen.servacc.exception.ServerAccessException;
import ru.naumen.servacc.settings.ListProvider;
import ru.naumen.servacc.settings.impl.DefaultConfiguration;
import ru.naumen.servacc.util.FileUtil;

/**
 * @author Andrey Hitrin
 * @since 31.01.13
 */
public class OS
{
    private static final GUIOptions MAC_OS_X_LOOK = new GUIOptions(false, true, true);
    private static final GUIOptions UBUNTU_LOOK = new GUIOptions(false, true, true);
    private static final GUIOptions MAINSTREAM_LOOK = new GUIOptions(true, false, false);

    private final Platform platform;
    private final DefaultConfiguration configuration;
    private final GUIOptions guiOptions;

    public OS()
    {
        if (isMacOSX())
        {
            platform = new MacOsX();
            guiOptions = MAC_OS_X_LOOK;
        }
        else if (isWindows())
        {
            platform = new Windows();
            guiOptions = MAINSTREAM_LOOK;
        }
        else if (isUnityDesktop() | isGnomeDesktop())
        {
            platform = new Linux();
            guiOptions = UBUNTU_LOOK;
        }
        else
        {
            platform = new Linux();
            guiOptions = MAINSTREAM_LOOK;
        }
        configuration = DefaultConfiguration.create(platform.getConfigDirectory());
    }

    private static boolean isMacOSX()
    {
        return "Mac OS X".equalsIgnoreCase(System.getProperty("os.name"));
    }

    private static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static boolean isUnityDesktop()
    {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        return desktop != null && desktop.startsWith("Unity");
    }

    private static boolean isGnomeDesktop()
    {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        return "ubuntu:GNOME".equals(desktop);
    }

    public GUIOptions getGUIOptions()
    {
        return guiOptions;
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

    public File getKeyStoreDirectory()
    {
        return platform.getKeyStoreDirectory();
    }
    
    public File getTempKeyStoreDirectory()
    {
        return platform.getTempKeyStoreDirectory();
    }
    
    public void initTempDirectories()
    {
        removeTempDirectories();
        platform.getTempKeyStoreDirectory().mkdir();
    }
    
    public void removeTempDirectories()
    {
        try
        {
            FileUtil.deleteDirReqursivelyIfExists(platform.getTempKeyStoreDirectory());
        }
        catch (IOException e)
        {
            throw new ServerAccessException(e);
        }
    }
}
