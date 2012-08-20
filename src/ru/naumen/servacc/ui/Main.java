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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.Backend;
import ru.naumen.servacc.platform.Platform;
import ru.naumen.servacc.settings.ApplicationProperties;
import ru.naumen.servacc.settings.DefaultConfiguration;
import ru.naumen.servacc.settings.ShellConfiguration;
import ru.naumen.servacc.util.Util;

public class Main implements Runnable
{
    private static final String WINDOW_HEADER = "Server Access";
    private static final String WINDOW_ICON = "/prog.ico";

    public static void main(String[] args) throws Exception
    {
        new Main().run();
    }

    public void run()
    {
        // Create GUI
        Platform platform = Util.platform();
        DefaultConfiguration configuration = DefaultConfiguration.create(platform);

        Display display = new Display();
        Shell shell = createShell(display, configuration.getWindowProperties());
        UIController controller = new UIController(shell, platform, new Backend(platform), configuration.sourceListProvider());
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

    private Shell createShell(Display display, ApplicationProperties windowProperties)
    {
        Shell shell = new Shell(display);
        shell.setText(WINDOW_HEADER);
        shell.setImage(ImageCache.getImage(WINDOW_ICON, 1));
        shell.setLayout(new GridLayout());
        final ShellConfiguration config = new ShellConfiguration(shell, windowProperties);
        // dispose handler
        shell.addListener(SWT.Dispose, new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                config.storePosition();
            }
        });
        config.restorePosition();
        return shell;
    }
}
