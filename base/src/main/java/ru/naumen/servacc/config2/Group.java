/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import java.util.ArrayList;
import java.util.List;

import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.util.Util;

public class Group implements IConfigItem
{
    private String name;
    private String comment;
    private List<IConfigItem> children;

    public Group()
    {
        this("", "");
    }

    public Group(String name, String comment)
    {
        this.comment = comment;
        this.name = name;
        children = new ArrayList<>();
    }

    public String getName()
    {
        return name;
    }

    public List<IConfigItem> getChildren()
    {
        return children;
    }

    public String toString()
    {
        String result = name;
        if (!Util.isEmptyOrNull(comment)) {
            result += " (" + comment + ")";
        }
        return result;
    }

    public boolean matches(String filter)
    {
        for (String s : new String[]{ name, comment })
        {
            if (!Util.isEmptyOrNull(s) && Util.matches(s, filter))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getIconName()
    {
        return "/icons/folder-horizontal.png";
    }
}
