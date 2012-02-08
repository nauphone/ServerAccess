package ru.naumen.servacc.platform;

import java.io.File;
import java.io.IOException;

/**
 * Hides platorm-specific code behind abstraction.
 * @author Andrey Hitrin
 * @since 08.02.2012
 */
public interface Platform
{
    void openTerminal(Object[] params) throws IOException;

    void openFTPBrowser(Object[] params) throws IOException;

    void openInBrowser(String url) throws IOException;

    File getConfigFile() throws IOException;

    boolean isTraySupported();

    boolean needToNegotiateProtocolOptions();

    boolean useSystemSearchWidget();

    boolean displayFolderIcon();
}
