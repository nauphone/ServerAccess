/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.settings.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.naumen.servacc.FileResource;

/**
 * @author Andrey Hitrin
 * @since 31.08.12
 */
public class DefaultPropertiesFile implements DefaultFile
{
    @Override
    public void fillWithDefaultContentIfNotExists(File configFile) throws IOException
    {
        if (configFile.createNewFile())
        {
            File accountsFile = new File(configFile.getParentFile(), "accounts.xml");
            String path = accountsFile.getCanonicalPath().replaceAll("\\\\", "/");
            new FileOutputStream(configFile).write(("source=" + FileResource.uriPrefix + path).getBytes("UTF-8"));
        }
    }
}
