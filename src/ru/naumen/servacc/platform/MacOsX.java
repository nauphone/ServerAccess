/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */
package ru.naumen.servacc.platform;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class MacOsX implements Platform
{
    @Override
    public void openFTPBrowser(int localPort) throws IOException
    {
        new ProcessBuilder("open", MessageFormat.format("ftp://anonymous@127.0.0.1:{0,number,#}", localPort)).start();
    }

    @Override
    public void openInBrowser(String url) throws IOException
    {
        // open URL in default browser
        new ProcessBuilder("open", url).start();
    }

    @Override
    public File getConfigDirectory() throws IOException
    {
        final String userHome = System.getProperty("user.home");
        File appDirectory = new File(userHome, "Library/Application Support");
        appDirectory = new File(appDirectory, "Server Access");
        return appDirectory;
    }

    @Override
    public boolean isTraySupported()
    {
        return false;
    }

    @Override
    public boolean isAppMenuSupported()
    {
        return true;
    }

    @Override
    public boolean useSystemSearchWidget()
    {
        return true;
    }

}
