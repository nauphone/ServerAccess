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

public final class Util
{
    private Util()
    {
        // Utility class should not have public constructor
    }

    public static boolean isEmptyOrNull(String str)
    {
        return str == null || str.trim().length() == 0;
    }

    public static boolean matches(String string, String filter)
    {
        String regexp = "(?iu).*" + Pattern.quote(filter) + ".*";
        return string.matches(regexp);
    }
}
