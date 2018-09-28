/*
 * Copyright (C) 2014 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.exception;

/**
 * @author Andrey Hitrin
 * @since 16.05.14
 */
public class ServerAccessException extends RuntimeException
{
    public ServerAccessException()
    {
        super();
    }

    public ServerAccessException(String message)
    {
        super(message);
    }

    public ServerAccessException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServerAccessException(Throwable cause)
    {
        super(cause);
    }
}
