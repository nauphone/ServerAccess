/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class AppProperties extends Properties
{
    private static final long serialVersionUID = 8932887805597715912L;
    private Boolean isXML = false;

    public void load(File file) throws IOException
    {
        try
        {
            this.loadFromXML(new FileInputStream(file));
        }
        catch (InvalidPropertiesFormatException e)
        {
            super.load(new FileInputStream(file));
        }
    }

    public void loadFromXML(InputStream in) throws IOException
    {
        super.loadFromXML(in);
        isXML = true;
    }

    public void store(File file) throws IOException
    {
        OutputStream os = new FileOutputStream(file);
        String comment = "";
        if (isXML)
        {
            super.storeToXML(os, comment);
        }
        else
        {
            super.store(os, comment);
        }
    }
}
