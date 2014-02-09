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
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.util.Util;

public class AuthenticationDialog extends DialogBase
{
    private Link link;
    private Text loginText;
    private Text passwordText;

    private String login;
    private String password;

    public AuthenticationDialog(Shell parent)
    {
        this(parent, false);
    }

    public AuthenticationDialog(Shell parent, boolean passwordOnly)
    {
        super(parent, 0, 2);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 2;
        createLabel("Your authentication is required to access the following resource.", gridData);

        createLabel("URL");
        link = createLink();

        if (!passwordOnly)
        {
            createLabel("Login");
            loginText = createTextField();
            loginText.setFocus();
        }

        createLabel("Password");
        passwordText = createTextField(SWT.PASSWORD);
        if (passwordOnly)
        {
            passwordText.setFocus();
        }

        createDefaultButton();
        pack();
    }

    public void setURL(String url)
    {
        if (!Util.isEmptyOrNull(url))
        {
            link.setText("<a href=\"" + url + "\">" + url + "</a>");
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
        login = loginText != null ? loginText.getText() : null;
        password = passwordText.getText();
        return true;
    }
}
