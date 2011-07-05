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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.Util;

public class LoginDialog extends DialogBase
{
    private Link link;
    private Text loginText;
    private Text passwordText;

    private String login;
    private String password;

    public LoginDialog(Shell parent)
    {
        this(parent, 0);
    }

    public LoginDialog(Shell parent, int style)
    {
        super(parent, style);
        shell.setLayout(new GridLayout(2, false));

        GridData gridData;

        Label description = new Label(shell, SWT.WRAP);
        description.setText("Your authentication is required to access the following resource.");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 2;
        description.setLayoutData(gridData);

        createLabel("URL");
        link = new Link(shell, SWT.NONE);
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

        createLabel("Login");
        loginText = new Text(shell, SWT.BORDER);
        loginText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        loginText.setFocus();

        createLabel("Password");
        passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Button button = new Button(shell, SWT.PUSH);
        button.setText("OK");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 2;
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

    public void setURL(String url)
    {
        if (!Util.isEmptyOrNull(url))
        {
            link.setText("<a href=\"" + url + "\">" + url + "</a>");
        }
    }

    public void setLogin(String login)
    {
        this.login = login;
        if (!Util.isEmptyOrNull(login))
        {
            loginText.setText(login);
        }
    }

    public void setPassword(String password)
    {
        this.password = password;
        if (!Util.isEmptyOrNull(password))
        {
            passwordText.setText(password);
        }
    }

    public String getLogin()
    {
        return login;
    }

    public String getPassword()
    {
        return password;
    }

    protected boolean validate()
    {
        login = loginText.getText();
        password = passwordText.getText();
        return true;
    }
}
