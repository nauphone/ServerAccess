/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.globalthrough;

import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.util.Util;

/**
 * This class is used to find all parents of the given account by its unique identity.
 *
 * @author Andrey Hitrin
 * @since 02.03.13
 */
public class Path
{
    private final SSHAccount account;
    private final String path;
    private final boolean found;

    public static Path foundAt(SSHAccount account, String path)
    {
        return new Path(account, path, true);
    }

    public static Path notFound()
    {
        return new Path(null, "", false);
    }

    public Path(SSHAccount account, String path, boolean found)
    {
        this.account = account;
        this.path = path;
        this.found = found;
    }

    public static Path find(IConfig config, String uniqueIdentity)
    {
        for (IConfigItem i : config.getChildren())
        {
            Path path = find(i, uniqueIdentity, "");
            if (path.found())
            {
                return path;
            }
        }
        return notFound();
    }

    private static Path find(IConfigItem object, String uniqueIdentity, String prefix)
    {
        if (object instanceof SSHAccount)
        {
            SSHAccount account = (SSHAccount) object;
            if (uniqueIdentity.equals(account.getUniqueIdentity()))
            {
                return foundAt(account, prefix + " > " + account);
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
                Path p = find(i, uniqueIdentity, newPrefix);
                if (p.found())
                {
                    return p;
                }
            }
        }
        return notFound();
    }

    public SSHAccount account()
    {
        return account;
    }

    public String path()
    {
        return path;
    }

    public boolean found()
    {
        return found;
    }
}
