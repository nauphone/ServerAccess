/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.globalthrough;

import ru.naumen.servacc.SSH2Backend;
import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.util.Util;

/**
 * Contains all logic around management of Global Through account.
 * Extracted from {@link ru.naumen.servacc.ui.UIController} in order to make this logic testable.
 *
 * @author Andrey Hitrin
 * @since 25.11.12
 */
public class GlobalThroughController
{
    private final GlobalThroughView view;
    // TODO there is duplication in globalThrough settings between this class and SSH2Backend
    //      we should try to condense it inside single class
    private final SSH2Backend backend;

    private String globalThroughUniqueIdentity;

    public GlobalThroughController(GlobalThroughView view, SSH2Backend backend)
    {
        this.view = view;
        this.backend = backend;
    }

    public void refresh(IConfig defaultConfig)
    {
        if (!Util.isEmptyOrNull(globalThroughUniqueIdentity))
        {
            select(globalThroughUniqueIdentity, defaultConfig);
        }
        else
        {
            clear();
        }
    }

    public void clear()
    {
        view.clearGlobalThroughWidget();
        globalThroughUniqueIdentity = null;
        backend.setGlobalThrough(null);
    }

    public void select(String uniqueIdentity, IConfig defaultConfig)
    {
        globalThroughUniqueIdentity = uniqueIdentity;
        canSelect(defaultConfig, uniqueIdentity, "");
    }

    private boolean canSelect(Object object, String uniqueIdentity, String prefix)
    {
        if (object instanceof SSHAccount)
        {
            SSHAccount account = (SSHAccount) object;
            if (uniqueIdentity.equals(account.getUniqueIdentity()))
            {
                String globalThroughText = prefix + " > " + account;
                view.setGlobalThroughWidget(globalThroughText);
                backend.setGlobalThrough(account);
                return true;
            }
        }
        else if (object instanceof IConfig)
        {
            for (IConfigItem i : ((IConfig) object).getChildren())
            {
                if (canSelect(i, uniqueIdentity, prefix))
                {
                    return true;
                }
            }
        }
        else if (object instanceof Group)
        {
            for (IConfigItem i : ((Group) object).getChildren())
            {
                String newPrefix = ((Group) object).getName();
                if (!Util.isEmptyOrNull(prefix))
                {
                    newPrefix = prefix + " > " + newPrefix;
                }
                if (canSelect(i, uniqueIdentity, newPrefix))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
