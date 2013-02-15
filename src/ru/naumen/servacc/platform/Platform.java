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

/**
 * Hides platform-specific code behind abstraction.
 * @author Andrey Hitrin
 * @since 08.02.2012
 */
public interface Platform
{
    void openFTPBrowser(int localPort) throws IOException;

    File getConfigDirectory() throws IOException;

    boolean isTraySupported();

    boolean isAppMenuSupported();

    boolean useSystemSearchWidget();

    Command defaultBrowser();

    Command defaultTerminal();
}
