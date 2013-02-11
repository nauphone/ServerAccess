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

import ru.naumen.servacc.util.Util;

/**
 * @author Andrey Hitrin
 * @since 08.02.12
 */
public class Windows implements Platform
{
    @Override
    public void openFTPBrowser(int localPort) throws IOException
    {
        new ProcessBuilder("explorer", MessageFormat.format("/n,ftp://127.0.0.1:{0,number,#}", localPort)).start();
    }

    @Override
    public void openInBrowser(String url) throws IOException
    {
        new ProcessBuilder("cmd", "/C", "start " + url).start();
    }

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
    public Terminal defaultTerminal()
    {
        return new Terminal("putty  {options}  -telnet  {host}  -P  {port}");
    }
}
