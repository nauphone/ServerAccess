/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ru.naumen.servacc.FileResource;

/**
 * Creates and manages application properties.
 *
 * @author Andrey Hitrin
 * @since 12.02.2012
 */
public class PropertiesFactory
{
    public AppProperties getAppProperties() throws Exception
    {
        AppProperties properties = new AppProperties();
        properties.load(getConfigFile());
        return properties;
    }

    public File getConfigFile() throws IOException
    {
        final String userHome = System.getProperty("user.home");
        File appDirectory = Util.platform().getConfigFile();
        appDirectory.mkdirs();
        File configFile = new File(appDirectory, "serveraccess.properties");
        if (configFile.createNewFile())
        {
            // generate default configuration
            String data;
            File accountsFile = new File(appDirectory, "accounts.xml");
            if (accountsFile.createNewFile())
            {
                data = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>"
                        + System.getProperty("line.separator") + "<Accounts version=\"2\"></Accounts>";
                new FileOutputStream(accountsFile).write(data.getBytes());
            }
            data = "source=" + FileResource.uriPrefix + accountsFile.getPath();
            new FileOutputStream(configFile).write(data.getBytes());
        }
        return configFile;
    }

    public Collection<String> getConfigSources() throws Exception
    {
        List<String> result = new ArrayList<String>();
        Properties properties = getAppProperties();
        String[] keys = properties.keySet().toArray(new String[] {});
        Arrays.sort(keys);
        for (String key : keys)
        {
            if (key.matches("source[0-9]*"))
            {
                result.add((String) properties.get(key));
            }
        }
        return result;
    }
}
