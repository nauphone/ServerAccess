/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.settings.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * @author Andrey Hitrin
 * @since 11.09.12
 */
public class FileCopy implements DefaultFile
{
    private static final Logger LOGGER = Logger.getLogger(FileCopy.class);
    private String source;

    public FileCopy(String source)
    {
        this.source = source;
    }

    @Override
    public void fill(File configFile) throws IOException
    {
        InputStream sourceStream = null;
        OutputStream targetStream = null;
        try
        {
            sourceStream = FileCopy.class.getResourceAsStream(source);
            targetStream = new FileOutputStream(configFile);
            int c;
            final int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            while ((c = sourceStream.read(buffer)) != -1)
            {
                targetStream.write(buffer, 0, c);
            }
        }
        finally
        {
            silentlyClose(sourceStream, "source");
            silentlyClose(targetStream, "target");
        }
    }

    private void silentlyClose(Closeable stream, String name) throws IOException
    {
        if (stream == null)
        {
            return;
        }
        try
        {
            stream.close();
        }
        catch (IOException e)
        {
            LOGGER.warn("Cannot silently close stream: " + name, e);
        }
    }
}
