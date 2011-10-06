/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.FileResource;
import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.config2.CompositeConfig;
import ru.naumen.servacc.config2.Config;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigLoader;
import ru.naumen.servacc.util.AppProperties;
import ru.naumen.servacc.util.StringEncrypter.EncryptionException;

public class ConfigLoader implements IConfigLoader
{
    private Map<String, String[]> authCache = new HashMap<String, String[]>();
    private final UIController controller;
    private final Shell shell;

    public ConfigLoader(UIController controller, Shell shell)
    {
        this.controller = controller;
        this.shell = shell;
    }

    public IConfig loadConfig() throws Exception
    {
        Properties properties = AppProperties.getAppProperties();
        CompositeConfig compositeConfig = new CompositeConfig();
        String[] keys = properties.keySet().toArray(new String[] {});
        Arrays.sort(keys);
        for (String key : keys)
        {
            if (key.matches("source[0-9]*"))
            {
                try
                {
                    IConfig config = loadConfig((String) properties.get(key));
                    if (config != null)
                    {
                        compositeConfig.add(config);
                    }
                }
                catch (Exception e)
                {
                    controller.showAlert(e.getLocalizedMessage());
                }
            }
        }
        return compositeConfig;
    }

    private IConfig loadConfig(String source) throws Exception
    {
        if (source.startsWith("http://") || source.startsWith("https://"))
        {
            return loadConfigViaHTTP(source);
        }
        else if (source.startsWith(FileResource.uriPrefix))
        {
            return loadConfigFromFile(source);
        }
        else
        {
            throw new RuntimeException("Unknown source type: " + source);
        }
    }

    private IConfig loadConfigViaHTTP(String url) throws Exception
    {
        HTTPResource resource = new HTTPResource(url);
        String[] auth = authCache.get(url);
        if (auth != null && auth.length == 2)
        {
            resource.setAuthentication(auth[0], auth[1]);
        }
        while (true)
        {
            try
            {
                return new Config(resource.getInputStream());
            }
            catch (HTTPResource.NotAuthenticatedError e)
            {
                ResourceDialog dialog = new ResourceDialog(shell, true);
                dialog.setURL(url);
                if (dialog.show())
                {
                    String login = dialog.getFieldValue("Login");
                    String password = dialog.getFieldValue("Password");
                    resource.setAuthentication(login, password);
                    authCache.put(url, new String[] {login, password});
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                resource.close();
            }
        }
    }

    private IConfig loadConfigFromFile(String source) throws Exception
    {
        InputStream stream = getConfigStream(source, shell);
        try
        {
            return stream==null?null:new Config(stream);
        }
        catch(Exception e)
        {
            stream.close();
            throw e;
        }
    }
    
    public static InputStream getConfigStream(String source, Shell shell) throws IOException
    {
        String password = null;
        while(true)
        {
            try
            {
                return FileResource.getConfigStream(source, password);
            }
            catch (EncryptionException e)
            {
                ResourceDialog dialog = new ResourceDialog(shell, false);
                dialog.setURL(source);
                if (dialog.show())
                    password = dialog.getFieldValue("Password");
                else
                    return null;
            }
        }
    }
}
