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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.Scanner;

import ru.naumen.servacc.util.StringEncrypter;
import ru.naumen.servacc.util.StringEncrypter.EncryptionException;


public class FileResource
{
    public static final String uriPrefix = "file://";
    public static final byte[] encryptedHeader = "RSACC".getBytes();

    public static InputStream getConfigStream(String uri, String password) throws IOException, EncryptionException
    {
        PushbackInputStream stream = openConfigStream(uri);
        try
        {
            if (!isConfigEncrypted(stream))
            {
                return stream;
            }

            if (password == null)
            {
                throw new EncryptionException("No password provided");
            }

            stream.skip(encryptedHeader.length);
            String content = new Scanner(stream).useDelimiter("\\A").next();
            stream.close();

            content = new StringEncrypter("DESede", password).decrypt(content);
            return new ByteArrayInputStream(content.getBytes());
        }
        catch (EncryptionException e)
        {
            stream.close();
            throw e;
        }
        catch (IOException e)
        {
            stream.close();
            throw e;
        }
    }

    public static boolean isConfigEncrypted(String config) throws IOException
    {
        PushbackInputStream stream = openConfigStream(config);
        try
        {
            return isConfigEncrypted(stream);
        }
        finally
        {
            stream.close();
        }
    }

    private static boolean isConfigEncrypted(PushbackInputStream stream) throws IOException
    {
        byte[] b = new byte[encryptedHeader.length];
        if (stream.read(b) != b.length)
        {
            throw new IOException("Failed to read accounts file");
        }
        stream.unread(b);
        return Arrays.equals(b, encryptedHeader);
    }

    private static PushbackInputStream openConfigStream(String uri) throws IOException
    {
        if (!uri.startsWith(uriPrefix))
        {
            throw new IOException("Bad accounts file resource prefix: " + uriPrefix);
        }
        return new PushbackInputStream(new FileInputStream(uri.substring(uriPrefix.length())), encryptedHeader.length);
    }
}
