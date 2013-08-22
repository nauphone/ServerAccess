/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.config2;

import ru.naumen.servacc.config2.i.IConfigItem;

/**
 * This class implements NullObject pattern for the interface {@link IConfigItem}.
 * Default behavior means:
 * <ul>
 *  <li>it doesn't match any search string</li>
 *  <li>it has no icon</li>
 * </ul>
 * @author Andrey Hitrin
 * @since 23.12.12
 */
public class EmptyConfigItem implements IConfigItem
{
    @Override
    public boolean matches(String filter)
    {
        return false;
    }

    @Override
    public String getIconName()
    {
        return null;
    }

    @Override
    public String toString() {
        return "";
    }
}
