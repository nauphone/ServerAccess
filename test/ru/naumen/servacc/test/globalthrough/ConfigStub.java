/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.globalthrough;

import java.util.Arrays;
import java.util.List;

import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;

/**
 * @author Andrey Hitrin
 * @since 02.03.13
 */
public class ConfigStub implements IConfig
{
    private List<IConfigItem> children;

    public ConfigStub(List<IConfigItem> children)
    {
        this.children = children;
    }

    public static IConfig config(IConfigItem... items)
    {
        return new ConfigStub(Arrays.asList(items));
    }

    public static IConfigItem group(String name, IConfigItem... items)
    {
        Group root = new Group(name, "");
        for (IConfigItem item : items)
        {
            root.getChildren().add(item);
        }
        return root;
    }

    public static IConfigItem httpAccount()
    {
        return new HTTPAccount();
    }

    @Override
    public List<IConfigItem> getChildren()
    {
        return children;
    }
}
