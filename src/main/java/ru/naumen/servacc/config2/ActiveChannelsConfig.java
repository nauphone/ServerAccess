/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import java.util.Arrays;
import java.util.List;

import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public class ActiveChannelsConfig implements IConfig
{
    private ActiveChannelsRegistry registry;

    public ActiveChannelsConfig(ActiveChannelsRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public List<IConfigItem> getChildren()
    {
        return Arrays.asList(registry);
    }
}
