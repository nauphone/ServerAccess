/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2.i;

/**
 * @author vtarasov
 * @since 22.01.2016
 */
public interface IConnectableConfigItem extends IConnectable, IConfigItem
{
    default String getConnectionProcessIconName()
    {
        return "/icons/connect.gif";
    }
}
