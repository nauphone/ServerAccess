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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.Backend;
import ru.naumen.servacc.util.AppProperties;
import ru.naumen.servacc.util.Util;

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
            controller = new UIController(shell, new Backend(Util.platform()));
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

    private void storeShellPosition(Shell shell) throws Exception
    {
        AppProperties properties = AppProperties.getAppProperties();
        Rectangle bounds = shell.getBounds();
        properties.setProperty(WINDOW_X, String.valueOf(bounds.x));
        properties.setProperty(WINDOW_Y, String.valueOf(bounds.y));
        properties.setProperty(WINDOW_WIDTH, String.valueOf(bounds.width));
        properties.setProperty(WINDOW_HEIGHT, String.valueOf(bounds.height));
        properties.store(AppProperties.getConfigFile());
    }

    private void restoreShellPosition(Shell shell) throws Exception
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
}
