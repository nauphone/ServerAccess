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
 */
public class Linux implements Platform
{
    @Override
    public void openTerminal(Object[] params) throws IOException
    {
        Runtime.getRuntime().exec(MessageFormat.format("putty {2} -telnet {0} -P {1,number,#}", params));
    }

    @Override
    public void openFTPBrowser(Object[] params) throws IOException
    {
        Runtime.getRuntime().exec(MessageFormat.format("gftp ftp://anonymous@{0}:{1,number,#}", params));
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
        return false;
    }

    @Override
    public boolean useSystemSearchWidget()
    {
        return false;
    }
}
