/*
 * Copyright (C) 2005-2015 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.backend;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A client could read bytes from channel and write bytes into it.
 * @author Andrey Hitrin
 * @since 14.10.15
 */
public interface DualChannel {
    OutputStream getOutputStream();
    InputStream getInputStream();
}
