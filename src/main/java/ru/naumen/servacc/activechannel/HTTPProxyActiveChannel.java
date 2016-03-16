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

import java.net.ServerSocket;
import java.net.Socket;

import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class HTTPProxyActiveChannel extends SocketActiveChannel
{
    public HTTPProxyActiveChannel(IActiveChannelThrough parent, ActiveChannelsRegistry registry, Socket socket, ServerSocket server)
    {
        super(parent, registry, socket, server);
    }

    @Override
    public String getId()
    {
        return String.valueOf(getSocket().getPort());
    }

    @Override
    public boolean matches(String filter)
    {
        return true;
    }

    @Override
    public String getIconName()
    {
        return "/icons/proxy-server.png";
    }

    @Override
    public String toString()
    {
        return "ports: " + getSocket().getPort() + " to " + getServer().getLocalPort() + " (http-proxy)";
    }
}
