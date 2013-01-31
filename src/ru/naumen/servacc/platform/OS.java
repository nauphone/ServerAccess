package ru.naumen.servacc.platform;

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

    public static Terminal terminal()
    {
        if (isMacOSX())
        {
            return new MacOsXTerminal();
        }
        return new Putty();
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
