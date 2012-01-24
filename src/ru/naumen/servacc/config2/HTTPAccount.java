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

import ru.naumen.servacc.config2.i.IConnectable;
import ru.naumen.servacc.util.Util;

public class HTTPAccount extends Account implements IConnectable
{
    public static String HTTPACCOUNT_TYPE = "http";
    public static String HTTPACCOUNT_PARAM_URL = "url";

    public String getURL()
    {
        return params.get(HTTPACCOUNT_PARAM_URL);
    }

    public String toString()
    {
        String result;
        String url = params.get("url");
        result = url;
        String login = params.get(ACCOUNT_PARAM_LOGIN);
        if (!Util.isEmptyOrNull(login))
        {
            result = login + " @ " + result;
        }
        return result;
    }

    @Override
    public boolean matches(String filter)
    {
        String login = params.get(ACCOUNT_PARAM_LOGIN);
        String url = params.get("url");
        for (String s : new String[] {name, comment, url, login})
        {
            if (!Util.isEmptyOrNull(s) && Util.matches(s, filter))
            {
                return true;
            }
        }
        return false;
    }
}
