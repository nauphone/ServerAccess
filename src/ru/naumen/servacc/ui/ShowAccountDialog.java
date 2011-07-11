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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ShowAccountDialog {
    private Shell shell;
    private Text passwordText;

    private boolean result = false;

    public ShowAccountDialog(final Shell shell, String text)
    {
        this(shell, 0);
        passwordText.setText(text);
    }

    public ShowAccountDialog(Shell parent, int style)
    {
        shell = new Shell(parent, SWT.SHEET | style);
        shell.setLayout(new GridLayout(2, false));

        GridData gridData;

        Label description = new Label(shell, SWT.WRAP);
        description.setText("Account information");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 2;
        description.setLayoutData(gridData);

        passwordText = new Text(shell, SWT.BORDER);
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        passwordText.setEditable(false);

        Button button = new Button(shell, SWT.PUSH);
        button.setText("OK");
        shell.setDefaultButton(button);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        button.setLayoutData(gridData);
        button.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                shell.close();
            }
        });
        shell.pack();
        shell.setSize(Math.max(shell.getSize().x, 400), shell.getSize().y);
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

	public boolean run()
    {
        shell.open();
        Display display = Display.findDisplay(Thread.currentThread());
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return result;
    }
}
