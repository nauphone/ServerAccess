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

import ru.naumen.servacc.util.Util;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class Windows implements Platform
{
    @Override
    public File getConfigDirectory() throws IOException
    {
        String appData = System.getenv("APPDATA");
        if (!Util.isEmptyOrNull(appData))
        {
            return new File(new File(appData), "Server Access");
        }
        return null;
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
    public boolean useSystemSearchWidget()
    {
        return false;
    }

    @Override
    public Command defaultBrowser()
    {
        return new Command("cmd  /C  start {url}");
    }

    @Override
    public Command defaultFTPBrowser()
    {
        return new Command("explorer  /n,ftp://{host}:{port}");
    }

    @Override
    public Command defaultTerminal()
    {
        return new Command("putty  {options}  -telnet  {host}  -P  {port}");
    }
}
