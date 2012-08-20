/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.settings;

import java.io.IOException;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class ShellConfiguration
{
    private static final String WINDOW_X = "window.x";
    private static final String WINDOW_Y = "window.y";
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";

    private Shell shell;
    private ApplicationProperties propertiesProvider;

    public ShellConfiguration(Shell shell, ApplicationProperties propertiesProvider)
    {
        this.shell = shell;
        this.propertiesProvider = propertiesProvider;
    }

    public void storePosition()
    {
        PropertiesFile propertiesFile = propertiesProvider.getAppProperties();
        Rectangle bounds = shell.getBounds();
        propertiesFile.setProperty(WINDOW_X, String.valueOf(bounds.x));
        propertiesFile.setProperty(WINDOW_Y, String.valueOf(bounds.y));
        propertiesFile.setProperty(WINDOW_WIDTH, String.valueOf(bounds.width));
        propertiesFile.setProperty(WINDOW_HEIGHT, String.valueOf(bounds.height));
        try
        {
            propertiesFile.store();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void restorePosition()
    {
        PropertiesFile propertiesFile = propertiesProvider.getAppProperties();
        Rectangle bounds = shell.getBounds();
        shell.setBounds(
                Integer.parseInt(propertiesFile.getProperty(WINDOW_X, String.valueOf(bounds.x))),
                Integer.parseInt(propertiesFile.getProperty(WINDOW_Y, String.valueOf(bounds.y))),
                Integer.parseInt(propertiesFile.getProperty(WINDOW_WIDTH, String.valueOf(bounds.width))),
                Integer.parseInt(propertiesFile.getProperty(WINDOW_HEIGHT, String.valueOf(bounds.height)))
        );
    }
}
