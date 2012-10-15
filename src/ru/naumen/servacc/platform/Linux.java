package ru.naumen.servacc.platform;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.lang.ProcessBuilder;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class Linux implements Platform
{
    @Override
    public void openTerminal(Object[] params) throws IOException
    {
    ProcessBuilder subp = new ProcessBuilder(
        "guake",
        "-n",
        "1",
        "-e",
        MessageFormat.format("telnet {0} {1,number,#}",params));
    subp.start();
    }

    @Override
    public void openFTPBrowser(Object[] params) throws IOException
    {
        Runtime.getRuntime().exec(MessageFormat.format("xdg-open ftp://anonymous@{0}:{1,number,#}", params));
    }

    @Override
    public void openInBrowser(String url) throws IOException
    {
        Runtime.getRuntime().exec("xdg-open " + url);
    }

    @Override
    public File getConfigDirectory() throws IOException
    {
        final String userHome = System.getProperty("user.home");
        return new File(userHome, ".serveraccess");
    }

    @Override
    public boolean isTraySupported()
    {
        return true;
    }

    @Override
    public boolean isAppMenuSupported()
    {
        return false;
    }

    @Override
    public boolean needToNegotiateProtocolOptions()
    {
        return true;
    }

    @Override
    public boolean useSystemSearchWidget()
    {
        return false;
    }

    @Override
    public boolean displayFolderIcon()
    {
        return true;
    }
}
