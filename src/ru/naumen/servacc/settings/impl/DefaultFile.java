/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.settings.impl;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrey Hitrin
 * @since 31.08.12
 */
public interface DefaultFile
{
    void fill(File configFile) throws IOException;
}
