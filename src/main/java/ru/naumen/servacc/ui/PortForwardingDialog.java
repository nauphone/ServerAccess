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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.SocketUtils;
import ru.naumen.servacc.util.Util;

public class PortForwardingDialog extends DialogBase
{
    private Label localPortLabel;
    private Label localHostLabel;
    private Label remotePortLabel;
    private Label remoteHostLabel;

    private Text localPortText;
    private Text localHostText;
    private Text remotePortText;
    private Text remoteHostText;

    private int localPort = -1;
    private int remotePort = -1;
    private String localHost = SocketUtils.LOCALHOST;
    private String remoteHost = SocketUtils.LOCALHOST;

    public PortForwardingDialog(Shell parent) throws IOException
    {
        super(parent, 4);

        localHostLabel = createLabel("Local Host");
        localPortLabel = createLabel("Local Port");
        remoteHostLabel = createLabel("Remote Host");
        remotePortLabel = createLabel("Remote Port");

        localHostText = createTextField();
        localPortText = createTextField();
        remoteHostText = createTextField();
        remotePortText = createTextField();
        remotePortText.setFocus();

        localHostText.setText(localHost);
        remoteHostText.setText(remoteHost);
        localPort = SocketUtils.getFreePort();
        localPortText.setText(String.valueOf(localPort));

        createDefaultButton();
        pack();
    }

    public int getLocalPort()
    {
        return localPort;
    }

    public String getLocalHost()
    {
        return localHost;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public String getRemoteHost()
    {
        return remoteHost;
    }

    protected Label createLabel(String text)
    {
        return createLabel(text, new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
    }

    protected boolean validate()
    {
        final Color red = getSystemColor(SWT.COLOR_RED);
        final Color fg = getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        boolean result = true;

        try
        {
            localPort = Integer.parseInt(localPortText.getText());
        }
        catch (NumberFormatException | NullPointerException e)
        {
            localPort = -1;
        }
        if (1 > localPort || localPort > 65535)
        {
            localPortLabel.setForeground(red);
            result = false;
        }
        else
        {
            localPortLabel.setForeground(fg);
        }

        try
        {
            remotePort = Integer.parseInt(remotePortText.getText());
        }
        catch (NumberFormatException | NullPointerException e)
        {
            remotePort = -1;
        }
        if (1 > remotePort || remotePort > 65535)
        {
            remotePortLabel.setForeground(red);
            result = false;
        }
        else
        {
            remotePortLabel.setForeground(fg);
        }

        localHost = localHostText.getText();
        if (Util.isEmptyOrNull(localHost))
        {
            localHostLabel.setForeground(red);
            result = false;
        }
        else
        {
            localHostLabel.setForeground(fg);
        }

        remoteHost = remoteHostText.getText();
        if (Util.isEmptyOrNull(remoteHost))
        {
            remoteHostLabel.setForeground(red);
            result = false;
        }
        else
        {
            remoteHostLabel.setForeground(fg);
        }
        return result;
    }
}
