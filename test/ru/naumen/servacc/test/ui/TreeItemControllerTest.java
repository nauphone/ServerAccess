/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.ui;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import ru.naumen.servacc.ui.TreeItemController;

/**
 * @author Andrey Hitrin
 * @since 23.12.12
 */
public class TreeItemControllerTest
{
    @Test
    public void doesntMatchStringByDefault()
    {
        TreeItemController controller = new TreeItemController();
        assertThat(controller.matches("some string"), is(false));
    }
}
