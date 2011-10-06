/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import ru.naumen.servacc.FileResource;

public class AppProperties extends Properties
{
    private static final long serialVersionUID = 8932887805597715912L;
    private Boolean isXML = false;

    public void load(File file) throws IOException
    {
        try
        {
            this.loadFromXML(new FileInputStream(file));
        }
        catch (InvalidPropertiesFormatException e)
        {
            super.load(new FileInputStream(file));
        }
    }

    public void loadFromXML(InputStream in) throws IOException
    {
        super.loadFromXML(in);
        isXML = true;
    }

    public void store(File file) throws IOException
    {
        OutputStream os = new FileOutputStream(file);
        String comment = "";
        if (isXML)
        {
            super.storeToXML(os, comment);
        }
        else
        {
            super.store(os, comment);
        }
    }

    public static AppProperties getAppProperties() throws Exception
    {
        AppProperties properties = new AppProperties();
        properties.load(getConfigFile());
        return properties;
    }

    public static File getConfigFile() throws IOException
    {
        final String userHome = System.getProperty("user.home");
        File appDirectory = null;
        if (Util.isMacOSX())
        {
            appDirectory = new File(userHome, "Library/Application Support");
            appDirectory = new File(appDirectory, "Server Access");
        }
        else if (Util.isWindows())
        {
            String appData = System.getenv("APPDATA");
            if (!Util.isEmptyOrNull(appData))
            {
                appDirectory = new File(appData);
                appDirectory = new File(appDirectory, "Server Access");
            }
        }
        // Linux and everything else
        if (appDirectory == null)
        {
            appDirectory = new File(userHome, ".serveraccess");
        }
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

    public static Collection<String> getConfigSources() throws Exception
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
