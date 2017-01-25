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

import java.util.HashMap;
import java.util.Map;

import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.util.Util;

public class Account implements IConfigItem
{
    public static final String ACCOUNT_PARAM_ADDRESS = "address";
    public static final String ACCOUNT_PARAM_LOGIN = "login";
    public static final String ACCOUNT_PARAM_PASSWORD = "password";

    private String type;
    private String id;
    private String name;
    private String comment;
    private Account through;
    private Map<String, String> params;

    public Account()
    {
        this.params = new HashMap<>();
    }

    public String getLogin()
    {
        return params.get(ACCOUNT_PARAM_LOGIN);
    }

    public String getPassword()
    {
        return params.get(ACCOUNT_PARAM_PASSWORD);
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Account getThrough()
    {
        return through;
    }

    public void setThrough(Account through)
    {
        this.through = through;
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    public void setParams(Map<String, String> params)
    {
        this.params = params;
    }

    public String toString()
    {
        String result = "(" + getType() + ")";
        String login = params.get(ACCOUNT_PARAM_LOGIN);
        String address = params.get(ACCOUNT_PARAM_ADDRESS);
        if (!Util.isEmptyOrNull(address))
        {
            if (!Util.isEmptyOrNull(login))
            {
                result += " " + login + " @ ";
            }
            result += address;
        }
        return result;
    }

    public boolean matches(String filter)
    {
        String login = params.get(ACCOUNT_PARAM_LOGIN);
        String address = params.get(ACCOUNT_PARAM_ADDRESS);
        for (String s : new String[]{ name, comment, address, login })
        {
            if (!Util.isEmptyOrNull(s) && Util.matches(s, filter))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getIconName()
    {
        return "/icons/card.png";
    }

    @Override
    public boolean isAccount() {
        return true;
    }
}
