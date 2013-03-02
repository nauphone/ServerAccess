/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test;

import ru.naumen.servacc.SSH2Backend;
import ru.naumen.servacc.config2.SSHAccount;

/**
 * @author Andrey Hitrin
 * @since 02.03.13
 */
public class SSH2BackendStub extends SSH2Backend
{
    public boolean cleared;
    public SSHAccount global;

    @Override
    public void setGlobalThrough(SSHAccount account)
    {
        global = account;
        cleared = (account == null);
    }
}
