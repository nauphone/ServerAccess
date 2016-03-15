/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Common code used in several test classes.
 *
 * @author Andrey Hitrin
 * @since 04.09.12
 */
public class FileUtils
{
    public static void write(File file, String contents) throws IOException
    {
        file.createNewFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(contents);
            writer.flush();
        }
    }

    public static List<String> contents(File file) throws IOException
    {
        List<String> contents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String s;
            while((s = reader.readLine()) != null)
            {
                contents.add(s);
            }
            return contents;
        }
    }
}
