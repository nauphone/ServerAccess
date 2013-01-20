/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Hitrin
 * @since 20.01.13
 */
public class Putty
{
    public void connect(int localPort, Map<String, String> params) throws IOException
    {
        String options = extractPuttyOptions(params);
        List<String> command = new ArrayList<String>();
        command.add("putty");
        String[] splitOptions = options.split(" ");
        for (String opt : splitOptions)
        {
            command.add(opt);
        }
        command.addAll(Arrays.asList("-telnet", "127.0.0.1", "-P", "" + localPort));
        new ProcessBuilder(command).start();
    }

    private String extractPuttyOptions(Map<String, String> params)
    {
        if (params.containsKey("putty_options"))
        {
            return params.get("putty_options");
        }
        return "";
    }
}
