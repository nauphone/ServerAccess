/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.activechannel.tasks;

import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class ActualizeActiveChannelsTask extends Thread
{
    private static final int ACTUALIZATION_CHECK_TIME = 300;

    private ActiveChannelsRegistry registry;

    public ActualizeActiveChannelsTask(ActiveChannelsRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void run()
    {
        while (registry.isRunning())
        {
            try
            {
                Thread.currentThread().sleep(ACTUALIZATION_CHECK_TIME);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }

            registry.actualizeAllChannels();
        }
    }
}
