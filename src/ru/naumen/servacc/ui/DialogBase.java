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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.util.Util;

public class DialogBase
{
    private final Shell shell;
    private boolean result = false;
    private int cols;

    public DialogBase(Shell parent, int style, int cols)
    {
        this.cols = cols;
        shell = new Shell(parent, SWT.SHEET | style);
        if (cols > 0)
        {
            shell.setLayout(new GridLayout(cols, false));
        }
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

    protected void createDefaultButton()
    {
        Button button = new Button(shell, SWT.PUSH);
        button.setText("OK");
        if (cols > 0)
        {
            GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
            gridData.horizontalSpan = cols;
            gridData.widthHint = 80;
            button.setLayoutData(gridData);
        }
        button.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                close(true);
            }
        });
        shell.setDefaultButton(button);
    }

    protected void pack()
    {
        shell.pack();
        shell.setSize(Math.max(shell.getSize().x, 400), shell.getSize().y);
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

    protected Text createTextField()
    {
        return createTextField(0);
    }

    protected Text createTextField(int flags)
    {
        final Text field = new Text(shell, SWT.BORDER | flags);
        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        return field;
    }

    protected Link createLink()
    {
        final Link link = new Link(shell, SWT.NONE);
        link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        link.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                if (!Util.isEmptyOrNull(e.text))
                {
                    Program.launch(e.text);
                }
            }
        });
        return link;
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

    protected Color getSystemColor(int colorCode)
    {
        return shell.getDisplay().getSystemColor(colorCode);
    }
}
