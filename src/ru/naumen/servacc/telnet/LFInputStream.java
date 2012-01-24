/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;


public class LFInputStream extends PushbackInputStream
{
    private int __length = 0;

    public LFInputStream(InputStream input)
    {
        super(input, 2);
    }


    private int __read() throws IOException
    {
        int ch;

        ch = super.read();

        if (ch == '\r')
        {
            ch = super.read();
            if (ch == '\n' || ch == 0)
            {
                unread("\n".getBytes());
                ch = super.read();
                // This is a kluge for read(byte[], ...) to read the right amount
                --__length;
            }
            else
            {
                if (ch != -1)
                    unread(ch);
                return '\r';
            }
        }

        return ch;
    }

    public int read() throws IOException
    {
        return __read();
    }

    public int read(byte buffer[]) throws IOException
    {
        return read(buffer, 0, buffer.length);
    }

    public int read(byte buffer[], int offset, int length) throws IOException
    {
        int ch, off;

        if (length < 1)
            return 0;

        ch = available();

        __length = (length > ch ? ch : length);

        // If nothing is available, block to read only one character
        if (__length < 1)
            __length = 1;

        if ((ch = __read()) == -1)
            return -1;

        off = offset;

        do
        {
            buffer[offset++] = (byte)ch;
        }
        while (--__length > 0 && (ch = __read()) != -1);


        return (offset - off);
    }

    public int available() throws IOException
    {
        return (buf.length - pos) + in.available();
    }
    
}
