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

import ru.naumen.servacc.config2.EmptyConfigItem;
import ru.naumen.servacc.config2.i.IConfigItem;

public class TreeItemController
{
    private final IConfigItem data;
    private final TreeItemController parent;

    private List<TreeItemController> children;

    private boolean visible = true;
    private boolean expanded = false;

    public TreeItemController()
    {
        this(null, new EmptyConfigItem());
    }

    public TreeItemController(TreeItemController parent, IConfigItem data)
    {
        this.parent = parent;
        this.data = data;
        children = new ArrayList<>();
    }

    public IConfigItem getData()
    {
        return data;
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

    public void raiseVisibility()
    {
        if (isExpanded() && isVisible()) {
            return;
        }
        setVisibility(true);
        setExpanded(true);
        if (hasParent())
        {
            parent.raiseVisibility();
        }
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

    private boolean uprisingMatches(String filter)
    {
        return dataMatches(filter) || parentMatches(filter);
    }

    private boolean parentMatches(String filter)
    {
        return hasParent() && parent.uprisingMatches(filter);
    }

    private boolean dataMatches(String filter)
    {
        return data.matches(filter);
    }

    private boolean hasParent() {
        return parent != null;
    }

    @Override
    public String toString()
    {
        return data.toString();
    }

    public String getImageName()
    {
        return data.getIconName();
    }
}
