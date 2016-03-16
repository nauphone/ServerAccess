/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author vtarasov
 * @since 10.03.16
 */
public class FileUtil
{
    private FileUtil()
    {
        // Utility class should not have public constructor
    }
    
    public static void deleteDirReqursively(File dir) throws IOException
    {
        if (dir.isDirectory())
        {
            Path directory = Paths.get(dir.getAbsolutePath());
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
                {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        }
    }
    
    public static void deleteDirReqursivelyIfExists(File dir) throws IOException
    {
        if (dir.exists())
        {
            deleteDirReqursively(dir);
        }
    }
}
