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
    public static String ACCOUNT_PARAM_ADDRESS = "address";
    public static String ACCOUNT_PARAM_LOGIN = "login";
    public static String ACCOUNT_PARAM_PASSWORD = "password";

    public String type;
    public String id;
    public String name;
    public String comment;
    public Account through;
    public Map<String, String> params;

    public Account()
    {
        params = new HashMap<String, String>();
    }

    public String getLogin()
    {
        return params.get(ACCOUNT_PARAM_LOGIN);
    }

    public String getPassword()
    {
        return params.get(ACCOUNT_PARAM_PASSWORD);
    }

    public String toString()
    {
        String result = "(" + type + ")";
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
        for (String s : new String[] {name, comment, address, login})
        {
            if (!Util.isEmptyOrNull(s) && Util.matches(s, filter))
            {
                return true;
            }
        }
        return false;
    }
}
