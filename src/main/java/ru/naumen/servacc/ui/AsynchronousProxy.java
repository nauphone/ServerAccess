/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.ui;

import org.eclipse.swt.widgets.Display;
import ru.naumen.servacc.MessageListener;

/**
 * @author Andrey Hitrin
 * @since 10.02.13
 */
public class AsynchronousProxy implements MessageListener
{
    private final MessageListener messageListener;

    public AsynchronousProxy(MessageListener messageListener)
    {
        this.messageListener = messageListener;
    }

    @Override
    public void notify(final String message)
    {
        Display.getDefault().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                messageListener.notify(message);
            }
        });
    }
}
