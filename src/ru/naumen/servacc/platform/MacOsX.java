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
import java.util.Map;

import org.eclipse.swt.program.Program;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class MacOsX implements Platform
{
    @Override
    public void openTerminal(int localPort, Map<String, String> params) throws IOException
    {
        Runtime.getRuntime().exec(MessageFormat.format("open telnet://127.0.0.1:{0,number,#}", localPort));
    }

    @Override
    public void openFTPBrowser(int localPort) throws IOException
    {
        Runtime.getRuntime().exec(MessageFormat.format("open ftp://anonymous@127.0.0.1:{0,number,#}", localPort));
    }

    @Override
    public void openInBrowser(String url) throws IOException
    {
        // open URL in default browser
        Program.launch(url);
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
    public boolean needToNegotiateProtocolOptions()
    {
        return true;
    }

    @Override
    public boolean useSystemSearchWidget()
    {
        return true;
    }
}
