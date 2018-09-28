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
    private int readLength = 0;

    public LFInputStream(InputStream input)
    {
        super(input, 2);
    }

    private int readInternal() throws IOException
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
                --readLength;
            }
            else
            {
                if (ch != -1)
                {
                    unread(ch);
                }
                return '\r';
            }
        }

        return ch;
    }

    @Override
    public int read() throws IOException
    {
        return readInternal();
    }

    @Override
    public int read(byte buffer[]) throws IOException
    {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte buffer[], int offset, int length) throws IOException
    {
        int ch;
        int end = offset;

        if (length < 1)
        {
            return 0;
        }

        ch = available();

        readLength = (length > ch ? ch : length);

        // If nothing is available, block to read only one character
        if (readLength < 1)
        {
            readLength = 1;
        }

        ch = readInternal();
        if (ch == -1)
        {
            return -1;
        }

        do
        {
            buffer[end++] = (byte) ch;
        }
        while (--readLength > 0 && (ch = readInternal()) != -1);


        return (end - offset);
    }

    @Override
    public int available() throws IOException
    {
        return (buf.length - pos) + in.available();
    }
}
