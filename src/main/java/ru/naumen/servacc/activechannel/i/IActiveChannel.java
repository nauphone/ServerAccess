/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.activechannel.i;

import java.util.List;

import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;
import ru.naumen.servacc.config2.i.IConfigItem;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public interface IActiveChannel extends IConfigItem
{
    IActiveChannelThrough getParent();

    String getId();
    List<String> getPath();

    void close();

    void save();
    void delete();

    boolean isActive();

    void accept(IActiveChannelVisitor visitor);
}
