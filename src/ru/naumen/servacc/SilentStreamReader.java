/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc;

import java.io.IOException;
import java.io.InputStream;

class SilentStreamReader implements Runnable
{
    private InputStream stream;

    public SilentStreamReader(InputStream stream)
    {
        super();
        this.stream = stream;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                stream.read();
            }
        }
        catch (IOException e)
        {
        }
    }
}
