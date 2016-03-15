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

import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public abstract class ActiveChannel implements IActiveChannel
{
    private IActiveChannelThrough parent;
    private ActiveChannelsRegistry registry;

    public ActiveChannel(IActiveChannelThrough parent, ActiveChannelsRegistry registry)
    {
        this.parent = parent;
        this.registry = registry;
    }

    @Override
    public IActiveChannelThrough getParent()
    {
        return parent;
    }

    @Override
    public List<String> getPath()
    {
        List<String> path = new ArrayList<String>();

        if (parent != null)
        {
            path.addAll(parent.getPath());
        }

        path.add(getId());

        return path;
    }

    @Override
    public void close()
    {
        delete();
    }

    @Override
    public void save()
    {
        registry.saveChannel(getPath(), this);
    }

    @Override
    public void delete()
    {
        registry.deleteChannel(this);
    }

    @Override
    public void accept(IActiveChannelVisitor visitor)
    {
        visitor.visit(this);
    }
}
