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

    public static final String base64encode(String string)
    {
        return base64encode(string.getBytes());
    }

    public static final String base64encode(byte[] bytes)
    {
        return new String(Base64.encode(bytes));
    }

    public static byte[] base64decode(String encryptedString)
    {
        return base64decode(encryptedString.getBytes());
    }

    public static byte[] base64decode(byte[] bytes)
    {
        return Base64.decode(bytes);
    }
}
