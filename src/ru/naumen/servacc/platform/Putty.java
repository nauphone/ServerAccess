/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.platform;

/**
 * @author Andrey Hitrin
 * @since 20.01.13
 */
public class Putty extends Terminal
{
    public Putty()
    {
        super("putty  {options}  -telnet  {host}  -P  {port}");
    }
}
