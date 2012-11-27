/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfigItem;

public class TreeItemController
{
    private TreeItemController parent;
    private IConfigItem data;
    private List<TreeItemController> children;

    private boolean visible = true;
    private boolean expanded = false;

    public TreeItemController()
    {
        this(null);
    }

    public TreeItemController(TreeItemController parent)
    {
        this.parent = parent;
        children = new ArrayList<TreeItemController>();
    }

    public TreeItemController getParent()
    {
        return parent;
    }

    public IConfigItem getData()
    {
        return data;
    }

    public void setData(IConfigItem data)
    {
        this.data = data;
    }

    public List<TreeItemController> getChildren()
    {
        return children;
    }

    public void setVisibility(boolean visibility)
    {
        visible = visibility;
    }

    public boolean isVisible()
    {
        return visible;
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }

    public boolean isExpanded()
    {
        return expanded;
    }

    public boolean matches(Collection<String> filters)
    {
        for (String f : filters)
        {
            if (!uprisingMatches(f))
            {
                return false;
            }
        }
        return true;
    }

    public boolean matches(String filter)
    {
        return data.matches(filter);
    }

    private boolean uprisingMatches(String filter)
    {
        if (matches(filter))
        {
            return true;
        }
        return (getParent() != null && getParent().uprisingMatches(filter));
    }

    public String toString()
    {
        return " " + data.toString();
    }

    public String getImageName()
    {
        if (data instanceof SSHAccount)
        {
            return "/icons/application-terminal.png";
        }
        else if (data instanceof HTTPAccount)
        {
            return "/icons/application-browser.png";
        }
        else if (data instanceof Account)
        {
            return "/icons/card.png";
        }
        else if (data instanceof Group)
        {
            return "/icons/folder-horizontal.png";
        }
        return null;
    }
}
