package ru.naumen.servacc.platform;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import ru.naumen.servacc.util.Util;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class Windows implements Platform
{
    public void openTerminal(Object[] params) throws IOException {
        Runtime.getRuntime().exec(MessageFormat.format("cmd /C start putty {2} -telnet {0} -P {1,number,#}", params));
    }

    public void openFTPBrowser(Object[] params) throws IOException {
        Runtime.getRuntime().exec(MessageFormat.format("cmd /C explorer /n,ftp://{0}:{1,number,#}", params));
    }

    public void openInBrowser(String url) throws IOException {
        Runtime.getRuntime().exec("cmd /C start " + url);
    }

    public File getConfigFile() throws IOException {
        String appData = System.getenv("APPDATA");
        if (!Util.isEmptyOrNull(appData))
        {
             return new File(new File(appData), "Server Access");
        }
        return null;
    }

    public boolean isTraySupported() {
        return true;
    }

    public boolean needToNegotiateProtocolOptions() {
        return false;
    }

    public boolean useSystemSearchWidget() {
        return false;
    }

    public boolean displayFolderIcon() {
        return true;
    }
}
