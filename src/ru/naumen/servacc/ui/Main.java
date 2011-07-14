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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.Backend;
import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.config2.CompositeConfig;
import ru.naumen.servacc.config2.Config;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigLoader;
import ru.naumen.servacc.util.AppProperties;

public class Main implements Runnable
{
    private final static String WINDOW_HEADER = "Server Access";
    private final static String WINDOW_ICON = "/prog.ico";

    private static final String WINDOW_X = "window.x";
    private static final String WINDOW_Y = "window.y";
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";

    public static void main(String[] args) throws Exception
    {
        new Main().run();
    }

    private Shell shell;
    private UIController controller;

    public void run()
    {
        try
        {
            // Create GUI
            Display display = new Display();
            shell = createShell(display);
            controller = new UIController(shell, new ConfigLoader(), new Backend());
            shell.open();
            // Load accounts
            controller.reloadConfig();

            while (!shell.isDisposed())
            {
                if (!display.readAndDispatch())
                {
                    display.sleep();
                }
            }
            display.dispose();
            controller.cleanup();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Shell createShell(Display display) throws Exception
    {
        final Shell shell = new Shell(display);
        shell.setText(WINDOW_HEADER);
        shell.setImage(UIController.getImage(WINDOW_ICON, 1));
        shell.setLayout(new GridLayout());
        // dispose handler
        shell.addListener(SWT.Dispose, new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                try
                {
                    storeShellPosition(shell);
                }
                catch (Exception e)
                {
                }
            }
        });
        restoreShellPosition(shell);
        return shell;
    }

    private static void storeShellPosition(Shell shell) throws Exception
    {
        AppProperties properties = AppProperties.getAppProperties();
        Rectangle bounds = shell.getBounds();
        properties.setProperty(WINDOW_X, String.valueOf(bounds.x));
        properties.setProperty(WINDOW_Y, String.valueOf(bounds.y));
        properties.setProperty(WINDOW_WIDTH, String.valueOf(bounds.width));
        properties.setProperty(WINDOW_HEIGHT, String.valueOf(bounds.height));
        properties.store(AppProperties.getConfigFile());
    }

    private static void restoreShellPosition(Shell shell) throws Exception
    {
        AppProperties properties = AppProperties.getAppProperties();
        Rectangle bounds = shell.getBounds();
        shell.setBounds(
            Integer.parseInt(properties.getProperty(WINDOW_X, String.valueOf(bounds.x))),
            Integer.parseInt(properties.getProperty(WINDOW_Y, String.valueOf(bounds.y))),
            Integer.parseInt(properties.getProperty(WINDOW_WIDTH, String.valueOf(bounds.width))),
            Integer.parseInt(properties.getProperty(WINDOW_HEIGHT, String.valueOf(bounds.height)))
        );
    }

    private class ConfigLoader implements IConfigLoader
    {
        private Map<String, String[]> authCache = new HashMap<String, String[]>();

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
            else if (source.startsWith("file://"))
            {
                source = source.substring("file://".length());
                File file = new File(source);
                if (!file.exists())
                {
                    throw new IOException("File '" + file.getAbsolutePath() + "' does not exist.");
                }
                return loadConfigFromFile(file);
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
                    LoginDialog dialog = new LoginDialog(shell);
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

        private IConfig loadConfigFromFile(File file) throws Exception
        {
            return new Config(new FileInputStream(file));
        }
    }
}
