/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import java.util.List;
import java.util.Vector;

import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.util.Util;

public class Group implements IConfigItem
{
    public String name;
    public String comment;
    public List<IConfigItem> children;

    public Group()
    {
        children = new Vector<IConfigItem>();
    }

    public Group(String name)
    {
        this();
        this.name = name;
    }

    public Group(String name, String comment)
    {
        this(name);
        this.comment = comment;
    }

    public List<IConfigItem> getChildren()
    {
        return children;
    }

    public String toString()
    {
        return name;
    }

    public boolean matches(String filter)
    {
        for (String s : new String[] {name, comment})
        {
            if (!Util.isEmptyOrNull(s) && Util.matches(s, filter))
            {
                return true;
            }
        }
        return false;
    }
}
