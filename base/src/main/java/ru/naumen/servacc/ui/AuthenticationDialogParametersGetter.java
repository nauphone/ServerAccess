/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.IAuthenticationParametersGetter;

/**
 * @author vtarasov
 * @since 15.03.16
 */
public class AuthenticationDialogParametersGetter implements IAuthenticationParametersGetter
{
    private String resourcePath;

    private String login;
    private String password;

    private final Shell shell;

    public AuthenticationDialogParametersGetter(Shell shell)
    {
        this.shell = shell;
    }

    @Override
    public void setResourcePath(String resourcePath)
    {
        this.resourcePath = resourcePath;
    }

    @Override
    public void doGet()
    {
        Display.getDefault().syncExec(() ->
            {
                AuthenticationDialog dialog = new AuthenticationDialog(shell);
                dialog.setURL(resourcePath);
                if (dialog.show())
                {
                    login = dialog.getLogin();
                    password = dialog.getPassword();
                }
            });
    }

    @Override
    public String getLogin()
    {
        return login;
    }

    @Override
    public String getPassword()
    {
        return password;
    }
}
