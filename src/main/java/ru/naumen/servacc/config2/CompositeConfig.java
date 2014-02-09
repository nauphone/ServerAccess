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

import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;

public class CompositeConfig implements IConfig
{
    private List<IConfigItem> children;

    public CompositeConfig()
    {
        children = new ArrayList<IConfigItem>();
    }

    public List<IConfigItem> getChildren()
    {
        return children;
    }

    public void add(IConfig config)
    {
        children.addAll(config.getChildren());
    }
}
