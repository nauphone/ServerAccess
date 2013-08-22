/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.config2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author Andrey Hitrin
 * @since 23.08.13
 */
public class SSHAccountTest
{
    @Test
    public void emptyAccountIsRepresentedAsEmptyString()
    {
        SSHAccount account = new SSHAccount();
        assertThat(account.toString(), is("*** empty ***"));
    }
}
