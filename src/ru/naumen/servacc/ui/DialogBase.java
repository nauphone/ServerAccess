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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DialogBase
{
    protected final Shell shell;
    protected boolean result = false;

    public DialogBase(Shell parent)
    {
        this(parent, 0);
    }

    public DialogBase(Shell parent, int style)
    {
        shell = new Shell(parent, SWT.SHEET | style);
    }

    public boolean show()
    {
        shell.open();
        while (!shell.isDisposed())
        {
            if (!shell.getDisplay().readAndDispatch())
            {
                shell.getDisplay().sleep();
            }
        }
        shell.dispose();
        return result;
    }

    protected Label createLabel(String text)
    {
        return createLabel(text, new GridData(GridData.HORIZONTAL_ALIGN_END));
    }

    protected Label createLabel(String text, GridData gridData)
    {
        Label label = new Label(shell, SWT.NONE);
        label.setLayoutData(gridData);
        label.setText(text);
        return label;
    }

    protected boolean validate()
    {
        return true;
    }

    protected void close(boolean result)
    {
        this.result = result;
        if (!result || validate())
        {
            shell.close();
        }
    }
}
