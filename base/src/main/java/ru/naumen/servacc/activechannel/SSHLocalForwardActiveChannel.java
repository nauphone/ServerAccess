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

import ru.naumen.servacc.SocketUtils;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class SSHLocalForwardActiveChannel extends ActiveChannel
{
    private int port;

    public SSHLocalForwardActiveChannel(SSHActiveChannel parent, ActiveChannelsRegistry registry, int port)
    {
        super(parent, registry);

        this.port = port;
    }

    @Override
    public String getId()
    {
        return String.valueOf(port);
    }

    @Override
    public boolean matches(String filter)
    {
        return true;
    }

    @Override
    public String getIconName()
    {
        return "/icons/port-forwarding.png";
    }

    @Override
    public void close()
    {
        super.close();

        // TODO: Remove local port forwarding from SSH client
    }

    @Override
    public String toString()
    {
        return "port: " + port + " (local forward)";
    }

    @Override
    public boolean isActive()
    {
        return !SocketUtils.isPortFree(port);
    }
}
