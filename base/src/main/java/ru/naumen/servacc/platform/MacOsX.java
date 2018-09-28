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

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class MacOsX implements Platform
{
    private final String userHome = System.getProperty("user.home");

    @Override
    public File getConfigDirectory()
    {
        File appDirectory = new File(userHome, "Library/Application Support");
        appDirectory = new File(appDirectory, "Server Access");
        return appDirectory;
    }

    @Override
    public File getKeyStoreDirectory()
    {
        return new File(userHome, ".ssh");
    }

    @Override
    public Command defaultBrowser()
    {
        return new Command("open  {url}");
    }

    @Override
    public Command defaultFTPBrowser()
    {
        return new Command("open  ftp://anonymous@{host}:{port}");
    }

    @Override
    public Command defaultTerminal()
    {
        return new Command("open  telnet://{host}:{port}");
    }
}
