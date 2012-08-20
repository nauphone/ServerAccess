/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.settings;

import java.util.Collection;

/**
 * @author Andrey Hitrin
 * @since 07.09.12
 */
public interface SourceListProvider
{
    Collection<String> list();
}
