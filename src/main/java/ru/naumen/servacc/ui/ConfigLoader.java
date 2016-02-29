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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.FileResource;
import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.MessageListener;
import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.config2.ActiveChannelsConfig;
import ru.naumen.servacc.config2.CompositeConfig;
import ru.naumen.servacc.config2.Config;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.exception.ServerAccessException;
import ru.naumen.servacc.settings.ListProvider;
import ru.naumen.servacc.util.StringEncrypter;
import ru.naumen.servacc.util.StringEncrypter.EncryptionException;
import ru.naumen.servacc.util.Util;

public class ConfigLoader
{
    private Map<String, String[]> authCache = new HashMap<>();
    private final Shell shell;
    private final ListProvider sourceListProvider;
    private final MessageListener listener;
    private final ActiveChannelsRegistry acRegistry;

    public ConfigLoader(Shell shell, ListProvider sourceListProvider, MessageListener listener, ActiveChannelsRegistry acRegistry)
    {
        this.shell = shell;
        this.sourceListProvider = sourceListProvider;
        this.listener = listener;
        this.acRegistry = acRegistry;
    }

    public IConfig loadConfig()
    {
        CompositeConfig compositeConfig = new CompositeConfig();
        Collection<String> sources = sourceListProvider.list();
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
                listener.notify(e.getLocalizedMessage());
            }
        }

        compositeConfig.add(new ActiveChannelsConfig(acRegistry));

        return compositeConfig;
    }

    private IConfig loadConfig(String source) throws Exception
    {
        if (source.startsWith("http://") || source.startsWith("https://"))
        {
            return loadConfigViaHTTP(source);
        }
        else if (source.startsWith(FileResource.URI_PREFIX))
        {
            return loadConfigFromFile(source);
        }
        else
        {
            throw new ServerAccessException("Unknown source type: " + source);
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
                    authCache.put(url, new String[]{ login, password });
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
        InputStream stream = getConfigStream(source);
        try
        {
            return stream == null ? null : new Config(stream);
        }
        catch (Exception e)
        {
            stream.close();
            throw e;
        }
    }

    private InputStream getConfigStream(String source) throws IOException
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
                    authCache.put(source, new String[]{ null, password });
                }
                else
                {
                    return null;
                }
            }
        }
    }

    public void encryptLocalAccounts()
    {
        try
        {
            Collection<String> configSources = sourceListProvider.list();

            int encryptableFiles = 0;
            for (String configURL : configSources)
            {
                if (!configURL.startsWith(FileResource.URI_PREFIX) || FileResource.isConfigEncrypted(configURL))
                {
                    continue;
                }
                encryptableFiles++;

                String password = askForPassword(configURL);

                if (Util.isEmptyOrNull(password))
                {
                    continue;
                }

                String content = new Scanner(getConfigStream(configURL)).useDelimiter("\\A").next();
                byte[] encryptedContent = new StringEncrypter("DESede", password).encrypt(content).getBytes();

                OutputStream os = new FileOutputStream(configURL.substring(FileResource.URI_PREFIX.length()));
                os.write(FileResource.ENCRYPTED_HEADER);
                os.write(System.getProperty("line.separator").getBytes());
                os.write(encryptedContent);
                os.close();
            }

            if (encryptableFiles < 1)
            {
                listener.notify("All accounts are already encrypted");
            }
        }
        catch (Exception e)
        {
            listener.notify(e.getMessage());
        }
    }

    private String askForPassword(String configURL)
    {
        EncryptDialog dialog = new EncryptDialog(shell);
        dialog.setURL(configURL);
        dialog.show();
        return dialog.getPassword();
    }

    public void decryptLocalAccounts()
    {
        try
        {
            Collection<String> configSources = sourceListProvider.list();

            int decryptableFiles = 0;
            for (String configURL : configSources)
            {
                String filePath = configURL.substring(FileResource.URI_PREFIX.length());
                if (!configURL.startsWith(FileResource.URI_PREFIX) || !FileResource.isConfigEncrypted(configURL))
                {
                    continue;
                }

                decryptableFiles++;

                InputStream stream = getConfigStream(configURL);
                if (stream == null)
                {
                    continue;
                }
                String content = new Scanner(stream).useDelimiter("\\A").next();
                stream.close();
                FileOutputStream os = new FileOutputStream(filePath);
                os.write(content.getBytes());
                os.close();
            }

            if (decryptableFiles < 1)
            {
                listener.notify("All accounts are already decrypted");
            }
        }
        catch (IOException e)
        {
            listener.notify(e.getMessage());
        }
    }
}
