/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.naumen.servacc.config2.i.IConnectableConfigItem;
import ru.naumen.servacc.util.Util;

public class SSHAccount extends Account implements IConnectableConfigItem
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SSHAccount.class);
    public static final String SSHACCOUNT_TYPE = "ssh";
    public static final String EMPTY_NAME = "*** empty ***";
    public static final int DEFAULT_SSH_PORT = 22;

    private String host;
    private Integer port;
    private String uniqueIdentity = null;
    private List<String> uniquePath = null;

    private void parseHostAndPort()
    {
        String newHost = getParams().get(ACCOUNT_PARAM_ADDRESS);
        Integer newPort = DEFAULT_SSH_PORT;
        String[] parts = newHost.split(":", 2);
        if (parts.length > 1)
        {
            newHost = parts[0];
            newPort = Integer.valueOf(parts[1]);
        }
        host = newHost;
        port = newPort;
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

    @Override
    public String toString()
    {
        String result = "";
        String address = getParams().get(ACCOUNT_PARAM_ADDRESS);
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
        if (!Util.isEmptyOrNull(getComment()))
        {
            result += " (" + getComment() + ")";
        }
        return result;
    }

    // TODO: Refactor to use getUniquePath() in the method body
    public String getUniqueIdentity()
    {
        if (uniqueIdentity == null)
        {
            uniqueIdentity = getSignature();
            // follow 'through' links
            SSHAccount cur = this;
            List<String> ids = new ArrayList<>();
            while (cur.getThrough() instanceof SSHAccount)
            {
                cur = (SSHAccount) cur.getThrough();
                if (ids.contains(cur.getId()))
                {
                    // circular reference detected
                    LOGGER.error("Circular reference detected: " + uniqueIdentity);
                    break;
                }
                ids.add(cur.getId());
                uniqueIdentity += " via " + cur.getSignature();
            }
        }
        return uniqueIdentity;
    }

    public List<String> getUniquePath()
    {
        if (uniquePath == null)
        {
            uniquePath = new ArrayList<>();

            uniquePath.add(getSignature());
            // follow 'through' links
            SSHAccount cur = this;
            List<String> ids = new ArrayList<>();
            while (cur.getThrough() instanceof SSHAccount)
            {
                cur = (SSHAccount) cur.getThrough();
                if (ids.contains(cur.getId()))
                {
                    // circular reference detected
                    LOGGER.error("Circular reference detected: " + uniqueIdentity);
                    break;
                }
                ids.add(cur.getId());
                uniquePath.add(cur.getSignature());
            }
        }
        return new ArrayList<>(uniquePath);
    }

    public List<String> getUniquePathReversed()
    {
        List<String> path = getUniquePath();
        Collections.reverse(path);
        return path;
    }

    public String getSignature()
    {
        String address = getParams().get(ACCOUNT_PARAM_ADDRESS);
        return "ssh://" + getLogin() + "@" + address;
    }

    @Override
    public String getIconName()
    {
        return "/icons/application-terminal.png";
    }

    public boolean needSudoLogin()
    {
        return getParams().containsKey("sudologin");
    }

    public SSHKey getSecureKey()
    {
        if (getParams().containsKey("rsaKey")) {

            final String path = getParams().get("rsaKey");
            final String pass = getParams().get("rsaPassword");
            return new SSHKey("ssh-rsa", path, pass);
        }
        else if(getParams().containsKey("dsaKey"))
        {
            final String path = getParams().get("dsaKey");
            final String pass = getParams().get("dsaPassword");
            return new SSHKey("ssh-dss", path, pass);
        }
        return null;
    }

    @Override
    public boolean isPortForwarder() {
        return true;
    }

    @Override
    public boolean isFtpBrowseable() {
        return true;
    }
}
