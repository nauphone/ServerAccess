/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.activechannel.visitors;

import ru.naumen.servacc.activechannel.i.IActiveChannel;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class ActualizeActiveChannelVisitor implements IActiveChannelVisitor
{
    @Override
    public void visit(IActiveChannel channel)
    {
        if (!channel.isActive())
        {
            channel.delete();
        }
    }
}
