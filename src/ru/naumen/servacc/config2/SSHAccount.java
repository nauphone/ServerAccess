/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import java.util.ArrayList;
import java.util.List;

import ru.naumen.servacc.config2.i.IConnectable;
import ru.naumen.servacc.config2.i.IFTPBrowseable;
import ru.naumen.servacc.config2.i.IPortForwarder;
import ru.naumen.servacc.util.Util;

public class SSHAccount extends Account implements IConnectable, IPortForwarder, IFTPBrowseable
{
    public static String SSHACCOUNT_TYPE = "ssh";
    public static String EMPTY_NAME = "*** empty ***";
    public static int DEFAULT_SSH_PORT = 22;

    private String host;
    private Integer port;
    private String uniqueIdentity = null;

    private void parseHostAndPort()
    {
        String host = params.get(ACCOUNT_PARAM_ADDRESS);
        Integer port = Integer.valueOf(DEFAULT_SSH_PORT);
        String[] parts = host.split(":", 2);
        if (parts.length > 1)
        {
            host = parts[0];
            port = Integer.valueOf(parts[1]);
        }
        this.host = host;
        this.port = port;
    }

    public String getHost()
    {
        if (host == null)
        {
            parseHostAndPort();
        }
        return host;
    }

    public Integer getPort()
    {
        if (port == null)
        {
            parseHostAndPort();
        }
        return port;
    }

    public String toString()
    {
        String result = "";
        String address = params.get(ACCOUNT_PARAM_ADDRESS);
        if (!Util.isEmptyOrNull(address))
        {
            if (!Util.isEmptyOrNull(getLogin()))
            {
                result += getLogin() + " @ ";
            }
            result += address;
        }
        else
        {
            result += EMPTY_NAME;
        }
        return result;
    }

    public String getUniqueIdentity()
    {
        if (uniqueIdentity == null)
        {
            uniqueIdentity = getSignature(this);
            // follow 'through' links
            SSHAccount cur = this;
            List<String> ids = new ArrayList<String>();
            while (cur.through instanceof SSHAccount)
            {
                cur = (SSHAccount) cur.through;
                if (ids.contains(cur.id))
                {
                    // circular reference detected
                    System.err.println("Circular reference detected: " + uniqueIdentity);
                    break;
                }
                ids.add(cur.id);
                uniqueIdentity += " via " + getSignature(cur);
            }
        }
        return uniqueIdentity;
    }

    private static String getSignature(SSHAccount account)
    {
        String address = account.params.get(ACCOUNT_PARAM_ADDRESS);
        String login = account.getLogin();
        return "ssh://" + login + "@" + address;
    }
}
