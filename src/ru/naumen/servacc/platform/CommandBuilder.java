/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Andrey Hitrin
 * @since 09.02.13
 */
public class CommandBuilder
{
    private final String command;

    public CommandBuilder(String command)
    {
        this.command = command;
    }

    public List<String> build(int port, Map<String, String> options)
    {
        List<String> result = new ArrayList<String>();
        String[] commandParts = command.
            replaceAll("[{]host[}]", "127.0.0.1").
            replaceAll("[{]port[}]", "" + port).
            split("  ");
        String optionsKey = commandParts[0] + "_options";
        for (String part : commandParts)
        {
            if ("{options}".equals(part))
            {
                result.addAll(extractOptions(options, optionsKey));
            }
            else if ("{name}".equals(part))
            {
                result.add(options.get("name"));
            }
            else
            {
                result.add(part);
            }
        }
        return result;
    }

    public List<String> build(String url)
    {
        String[] commandParts = command.
            replaceAll("[{]url[}]", Matcher.quoteReplacement(url)).
            split("  ");
        return Arrays.asList(commandParts);
    }

    private List<String> extractOptions(Map<String, String> options, String optionsKey)
    {
        if (options.containsKey(optionsKey))
        {
            return Arrays.asList(options.get(optionsKey).split(" "));
        }
        return new ArrayList<String>();
    }
}
