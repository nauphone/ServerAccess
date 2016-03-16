/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.activechannel;

import java.util.ArrayList;
import java.util.List;

import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public abstract class ActiveChannelThrough extends ActiveChannel implements IActiveChannelThrough
{
    private List<IActiveChannel> children = new ArrayList<IActiveChannel>();

    public ActiveChannelThrough(IActiveChannelThrough parent, ActiveChannelsRegistry registry)
    {
        super(parent, registry);
    }

    @Override
    public List<IActiveChannel> getChildren()
    {
        return getChildrenCopy();
    }

    private List<IActiveChannel> getChildrenCopy()
    {
        return new ArrayList<IActiveChannel>(children);
    }

    @Override
    public void addChild(IActiveChannel child)
    {
        children.add(child);
    }

    @Override
    public void removeChild(IActiveChannel child)
    {
        children.remove(child);
    }

    @Override
    public void accept(IActiveChannelVisitor visitor)
    {
        for (IActiveChannel child : getChildrenCopy())
        {
            child.accept(visitor);
        }

        super.accept(visitor);
    }
}
