/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.settings;

import java.io.File;
import java.io.IOException;

/**
 * Reads application properties.
 *
 * @author Andrey Hitrin
 * @since 12.02.2012
 */
public class ApplicationProperties
{
    private final File configFile;

    public ApplicationProperties(File configFile) throws IOException
    {
        this.configFile = configFile;
    }

    public PropertiesFile getAppProperties()
    {
        PropertiesFile propertiesFile = new PropertiesFile(configFile);
        try
        {
            propertiesFile.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);          // TODO use something better ;)
        }
        return propertiesFile;
    }
}
