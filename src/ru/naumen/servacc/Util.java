/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc;

import java.util.regex.Pattern;

import com.mindbright.util.Base64;

public class Util
{
    public static boolean isEmptyOrNull(String str)
    {
        return str == null || str.trim().length() == 0;
    }

    public static boolean matches(String string, String filter)
    {
        String regexp = "(?iu).*" + Pattern.quote(filter) + ".*";
        return string.matches(regexp);
    }

    public static boolean isMacOSX()
    {
        return "Mac OS X".equalsIgnoreCase(System.getProperty("os.name"));
    }

    public static boolean isLinux()
    {
        return "Linux".equalsIgnoreCase(System.getProperty("os.name"));
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static final String base64encode(String string)
    {
        return base64encode(string.getBytes());
    }

    public static final String base64encode(byte[] bytes)
    {
        return new String(Base64.encode(bytes));
    }
}
