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
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Andrey Hitrin
 * @since 03.02.13
 */
public class XTerm extends Terminal
{
    @Override
    public void connect(int localPort, Map<String, String> params) throws IOException
    {
        new ProcessBuilder("xterm", "-e", MessageFormat.format("telnet 127.0.0.1 {0,number,#}", localPort)).start();
    }
}
