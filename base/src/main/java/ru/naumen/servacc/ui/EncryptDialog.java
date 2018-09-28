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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.util.Util;

public class EncryptDialog extends DialogBase
{
    private Link link;
    private Text passwordText;
    private Text confirmText;
    private String password;

    public EncryptDialog(Shell parent)
    {
        super(parent, 2);

        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 2;
        createLabel("Please enter a new password for the following resource encryption.", gridData);

        createLabel("URL");
        link = createLink();

        createLabel("Password");
        passwordText = createTextField(SWT.PASSWORD);
        passwordText.setFocus();

        createLabel("Confirm");
        confirmText = createTextField(SWT.PASSWORD);

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

    public String getPassword()
    {
        return password;
    }

    protected boolean validate()
    {
        password = passwordText.getText();
        return !Util.isEmptyOrNull(password) && password.equals(confirmText.getText());
    }
}
