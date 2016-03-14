/**
 * 
 */
package ru.naumen.servacc.util;

import java.io.File;

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
    
    public static void deleteDirReqursively(File dir)
    {
        if (dir.isDirectory())
        {
            for (File subFile : dir.listFiles())
            {
                if (subFile.isFile())
                {
                    subFile.delete();
                }
                else
                {
                    deleteDirReqursively(subFile);
                }
            }
            dir.delete();
        }
    }
    
    public static void deleteDirReqursivelyIfExists(File dir)
    {
        if (dir.exists())
        {
            deleteDirReqursively(dir);
        }
    }
}
