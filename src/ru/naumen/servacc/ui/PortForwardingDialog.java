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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.util.Util;

public class PortForwardingDialog extends DialogBase
{
    private Label localPortLabel;
    private Label remotePortLabel;
    private Label remoteHostLabel;

    private Text localPortText;
    private Text remotePortText;
    private Text remoteHostText;

    private int localPort = -1;
    private int remotePort = -1;
    private String remoteHost;

    public PortForwardingDialog(Shell parent)
    {
        super(parent, 0, 3);

        localPortLabel = createLabel("Local Port");
        remoteHostLabel = createLabel("Remote Host");
        remotePortLabel = createLabel("Remote Port");

        localPortText = createTextField();
        remoteHostText = createTextField();
        remotePortText = createTextField();
        remotePortText.setFocus();

        createDefaultButton();
        pack();
    }

    public void setLocalPort(int port)
    {
        localPort = port;
        localPortText.setText(String.valueOf(port));
    }

    public void setRemoteHost(String host)
    {
        if (!Util.isEmptyOrNull(host))
        {
            remoteHost = host;
            remoteHostText.setText(host);
        }
    }

    public int getLocalPort()
    {
        return localPort;
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
        final Color red = shell.getDisplay().getSystemColor(SWT.COLOR_RED);
        final Color fg = shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        boolean result = true;

        localPort = -1;
        try
        {
            localPort = Integer.parseInt(localPortText.getText());
        }
        catch (NumberFormatException e)
        {
        }
        catch (NullPointerException e)
        {
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

        remotePort = -1;
        try
        {
            remotePort = Integer.parseInt(remotePortText.getText());
        }
        catch (NumberFormatException e)
        {
        }
        catch (NullPointerException e)
        {
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
