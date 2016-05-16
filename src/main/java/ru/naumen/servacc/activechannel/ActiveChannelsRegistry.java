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

import ru.naumen.servacc.activechannel.i.ActiveChannelsObservable;
import ru.naumen.servacc.activechannel.i.ActiveChannelsObserver;
import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.activechannel.i.IHidableChannel;
import ru.naumen.servacc.activechannel.tasks.ActualizeActiveChannelsTask;
import ru.naumen.servacc.activechannel.visitors.ActualizeActiveChannelVisitor;
import ru.naumen.servacc.activechannel.visitors.HideActiveChannelVisitor;
import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;
import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.i.IConfigItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public class ActiveChannelsRegistry extends Group implements ActiveChannelsObservable
{
    private List<ActiveChannelsObserver> observers = new ArrayList<ActiveChannelsObserver>();
    private boolean running = true;

    public ActiveChannelsRegistry()
    {
        super("Active channels", null);
        new ActualizeActiveChannelsTask(this).start();
    }

    @Override
    public boolean matches(String filter)
    {
        return true;
    }

    @Override
    public boolean isAutoExpanded()
    {
        return false;
    }

    @Override
    public String getIconName()
    {
        return "/icons/active-channels.png";
    }

    public void saveChannel(List<String> path, IActiveChannel channel)
    {
        if (existsChannel(path))
        {
            return;
        }

        if (channel.getParent() == null)
        {
            getChildren().add(channel);
        }
        else
        {
            channel.getParent().addChild(channel);
        }

        notifyActiveChannelsObservers();
    }

    public void deleteChannel(List<String> path)
    {
        IActiveChannel channel = findChannel(path);

        if (channel == null)
        {
            return;
        }

        deleteChannel(channel);
    }

    public void deleteChannel(IActiveChannel channel)
    {
        if (channel.getParent() == null)
        {
            getChildren().remove(channel);
        }
        else
        {
            channel.getParent().removeChild(channel);
        }

        notifyActiveChannelsObservers();
    }

    public boolean existsChannel(List<String> path)
    {
        return findChannel(path) != null;
    }

    public IActiveChannelThrough findChannelThrough(List<String> path)
    {
        IActiveChannel channel = findChannel(path);

        if (channel instanceof IActiveChannelThrough)
        {
            return (IActiveChannelThrough)channel;
        }

        return null;
    }

    public IActiveChannel findChannel(List<String> path)
    {
        List<IActiveChannel> channels = new ArrayList<IActiveChannel>();

        for (IConfigItem item : getChildren())
        {
            channels.add((IActiveChannel)item);
        }

        int childIndex = 0;
        int childrenLevel = 0;

        while (childIndex < channels.size())
        {
            if (childrenLevel == path.size())
            {
                return null;
            }

            IActiveChannel channel = channels.get(childIndex);

            if (channel instanceof IHidableChannel && ((IHidableChannel)channel).isHidden())
            {
                childIndex++;

                continue;
            }

            if (path.get(childrenLevel).equals(channel.getId()))
            {
                if (childrenLevel == path.size() - 1)
                {
                    return channel;
                }

                if (!(channel instanceof IActiveChannelThrough))
                {
                    return null;
                }

                channels = ((IActiveChannelThrough)channel).getChildren();
                childIndex = 0;
                childrenLevel++;

                continue;
            }

            childIndex++;
        }

        return null;
    }

    public void hideAllChannels()
    {
        forEachActiveChannel(new HideActiveChannelVisitor());
    }

    public void actualizeAllChannels()
    {
        forEachActiveChannel(new ActualizeActiveChannelVisitor());
    }

    private void forEachActiveChannel(IActiveChannelVisitor visitor)
    {
        new ArrayList<IConfigItem>(getChildren()).forEach(new Consumer<IConfigItem>()
        {
            @Override
            public void accept(IConfigItem t)
            {
                visitor.visit((IActiveChannel)t);
            }
        });
    }

    @Override
    public void addActiveChannelsObserver(ActiveChannelsObserver observer)
    {
        observers.add(observer);
    }

    @Override
    public void removeActiveChannelsObserver(ActiveChannelsObserver observer)
    {
        observers.remove(observer);
    }

    @Override
    public void notifyActiveChannelsObservers()
    {
        for (ActiveChannelsObserver observer : observers)
        {
            observer.activeChannelsChanged();
        }
    }

    public void finish() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
