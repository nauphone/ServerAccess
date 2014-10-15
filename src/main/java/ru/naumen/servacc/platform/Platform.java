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
 * Hides platform-specific code behind abstraction.
 *
 * @author Andrey Hitrin
 * @since 08.02.2012
 */
public interface Platform
{
    File getConfigDirectory();

    File getKeyStoreDirectory();

    Command defaultBrowser();

    Command defaultFTPBrowser();

    Command defaultTerminal();
}
