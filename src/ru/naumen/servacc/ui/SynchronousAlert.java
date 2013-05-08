/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import ru.naumen.servacc.MessageListener;

/**
 * @author Andrey Hitrin
 * @since 10.02.13
 */
public class SynchronousAlert implements MessageListener
{
    private final Shell shell;

    public SynchronousAlert(Shell shell)
    {
        this.shell = shell;
    }

    @Override
    public void notify(String text)
    {
        MessageBox mb = new MessageBox(shell, SWT.SHEET);
        mb.setMessage(text);
        mb.open();
    }
}
