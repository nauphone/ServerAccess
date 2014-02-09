/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.config2;

import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author Andrey Hitrin
 * @since 02.03.13
 */
public class SSHAccountStub extends SSHAccount
{
    private final String identity;
    public final String stringId;

    public SSHAccountStub(String identity, String stringId)
    {
        this.identity = identity;
        this.stringId = stringId;
    }

    @Override
    public String getUniqueIdentity()
    {
        return identity;
    }

    @Override
    public String toString()
    {
        return stringId;
    }
}
