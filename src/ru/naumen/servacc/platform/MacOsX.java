package ru.naumen.servacc.platform;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.swt.program.Program;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class MacOsX implements Platform
{
    public void openTerminal(Object[] params) throws IOException {
        Runtime.getRuntime().exec(MessageFormat.format("open telnet://{0}:{1,number,#}", params));
    }

    public void openFTPBrowser(Object[] params) throws IOException {
        Runtime.getRuntime().exec(MessageFormat.format("open ftp://anonymous@{0}:{1,number,#}", params));
    }

    public void openInBrowser(String url) throws IOException {
        Program.launch(url);
    }

    public File getConfigFile() throws IOException {
        final String userHome = System.getProperty("user.home");
        File appDirectory = new File(userHome, "Library/Application Support");
        appDirectory = new File(appDirectory, "Server Access");
        return appDirectory;
    }

    public boolean isTraySupported() {
        return false;
    }

    public boolean needToNegotiateProtocolOptions() {
        return true;
    }

    public boolean useSystemSearchWidget() {
        return true;
    }

    public boolean displayFolderIcon() {
        return false;
    }
}
