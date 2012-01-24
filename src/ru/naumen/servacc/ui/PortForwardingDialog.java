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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
        this(parent, 0);
    }

    public PortForwardingDialog(Shell parent, int style)
    {
        super(parent, style);
        shell.setLayout(new GridLayout(3, false));

        localPortLabel = createLabel("Local Port");
        remoteHostLabel = createLabel("Remote Host");
        remotePortLabel = createLabel("Remote Port");

        localPortText = new Text(shell, SWT.BORDER);
        localPortText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        remoteHostText = new Text(shell, SWT.BORDER);
        remoteHostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        remotePortText = new Text(shell, SWT.BORDER);
        remotePortText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        remotePortText.setFocus();

        Button button = new Button(shell, SWT.PUSH);
        button.setText("OK");
        GridData gridData;
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 3;
        gridData.widthHint = 80;
        button.setLayoutData(gridData);
        button.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                close(true);
            }
        });
        shell.setDefaultButton(button);
        shell.pack();
        shell.setSize(Math.max(shell.getSize().x, 400), shell.getSize().y);
    }

    public void setLocalPort(int port)
    {
        localPort = port;
        localPortText.setText(String.valueOf(port));
    }

    public void setRemotePort(int port)
    {
        remotePort = port;
        remotePortText.setText(String.valueOf(port));
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
