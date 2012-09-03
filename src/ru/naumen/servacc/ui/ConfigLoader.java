/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import ru.naumen.servacc.FileResource;
import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.config2.CompositeConfig;
import ru.naumen.servacc.config2.Config;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.util.ApplicationProperties;
import ru.naumen.servacc.util.StringEncrypter.EncryptionException;

public class ConfigLoader
{
    private Map<String, String[]> authCache = new HashMap<String, String[]>();
    private final UIController controller;
    private final Shell shell;
    private final ApplicationProperties applicationProperties;

    public ConfigLoader(UIController controller, Shell shell, ApplicationProperties applicationProperties )
    {
        this.controller = controller;
        this.shell = shell;
        this.applicationProperties = applicationProperties;
    }

    public IConfig loadConfig() throws Exception
    {
        CompositeConfig compositeConfig = new CompositeConfig();
        Collection<String> sources = applicationProperties.getConfigSources();
        for (String source : sources)
        {
            try
            {
                IConfig config = loadConfig(source);
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
                AuthenticationDialog dialog = new AuthenticationDialog(shell);
                dialog.setURL(url);
                if (dialog.show())
                {
                    String login = dialog.getLogin();
                    String password = dialog.getPassword();
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
            return stream == null ? null : new Config(stream);
        }
        catch(Exception e)
        {
            stream.close();
            throw e;
        }
    }

    public InputStream getConfigStream(String source, Shell shell) throws IOException
    {
        String password = null;
        String[] auth = authCache.get(source);
        if (auth != null && auth.length == 2)
        {
            password = auth[1];
        }
        while (true)
        {
            try
            {
                return FileResource.getConfigStream(source, password);
            }
            catch (EncryptionException e)
            {
                AuthenticationDialog dialog = new AuthenticationDialog(shell, true);
                dialog.setURL(source);
                if (dialog.show())
                {
                    password = dialog.getPassword();
                    authCache.put(source, new String[] {null, password});
                }
                else
                {
                    return null;
                }
            }
        }
    }
}
