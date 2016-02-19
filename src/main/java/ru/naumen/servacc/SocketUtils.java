/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SocketUtils
{
    public static final String LOCALHOST = "127.0.0.1";
    public static final int COLD_TIMEOUT = 15000;
    public static final int WARM_TIMEOUT = 1000;
    private static final int PORT_BASE = 12000;
    private static final int PORT_MAX = 13000;
    private static int port = PORT_BASE;

    private SocketUtils()
    {
        // Utility class should not have public constructor
    }

    public static ServerSocket createListener(String host) throws IOException
    {
        int portstart = port;
        while (true)
        {
            try
            {
                return new ServerSocket(++port, 0, InetAddress.getByName(host));
            }
            catch (IOException e)
            {
                if (port > PORT_MAX)
                {
                    port = PORT_BASE;
                }
                if (port == portstart)
                {
                    throw e;
                }
            }
        }
    }

    public static int getFreePort() throws IOException
    {
        ServerSocket sock = SocketUtils.createListener("0.0.0.0");
        int nextFreePort = sock.getLocalPort();
        sock.close();
        return nextFreePort;
    }

    public static List<String> getLocalAddresses() throws SocketException
    {
        List<String> addresses = new ArrayList<>();
        for (NetworkInterface i: Collections.list(NetworkInterface.getNetworkInterfaces()))
        {
            ArrayList<InetAddress> list = Collections.list(i.getInetAddresses());
            for (InetAddress address: list)
            {
                addresses.add(address.getHostAddress());
            }
        }
        return addresses;
    }
    
    public static boolean isPortFree(int port)
    {   
        try (ServerSocket server = new ServerSocket(port))
        {
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
