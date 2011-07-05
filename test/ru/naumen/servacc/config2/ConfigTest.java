/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;


public class ConfigTest
{
    static Config createConfigSafe(InputStream stream)
    {
        try
        {
            return new Config(stream);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConfigurationParser()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("test-config.xml");
        Config config = createConfigSafe(stream);
        Assert.assertEquals("There must be one group", config.getChildren().size(), 1);
        Group group = (Group)config.getChildren().get(0);
        Assert.assertTrue("There must be child group", group.getChildren().get(0) instanceof Group);
        group = (Group) group.getChildren().get(0);
        Assert.assertEquals("There must be 3 accounts", group.getChildren().size(), 3);

        Assert.assertTrue("First one is SSHAccount", group.getChildren().get(0) instanceof SSHAccount);
        SSHAccount sshAccount = (SSHAccount)group.getChildren().get(0);
        String NONAME = "*** empty ***";
        Assert.assertFalse("Account must have name", NONAME.equals(sshAccount.toString()));
        Assert.assertEquals("SSHAccount: attribute host", "127.0.0.1", sshAccount.getHost());
        Assert.assertEquals("SSHAccount: attribute port", Integer.valueOf(23), sshAccount.getPort());
        Assert.assertEquals("SSHAccount: attribute login", "foo", sshAccount.getLogin());
        Assert.assertEquals("SSHAccount: attribute password", "bar", sshAccount.getPassword());
        Assert.assertEquals("SSHAccount: attribute through", null, sshAccount.params.get("through"));

        Assert.assertTrue("Second one is Account", group.getChildren().get(1) instanceof Account);
        Account account = (Account)group.getChildren().get(1);
        Assert.assertEquals("Account: attribute login", "foo1", account.params.get("login"));
        Assert.assertEquals("Account: attribute password", "bar1", account.params.get("password"));
        Assert.assertEquals("SSHAccount: attribute through", "1", account.params.get("through"));

        Assert.assertTrue("Third one is HTTPAccount", group.getChildren().get(2) instanceof HTTPAccount);
        HTTPAccount httpAccount = (HTTPAccount)group.getChildren().get(2);
        Assert.assertEquals("HTTPAccount: attribute url", "http://127.0.0.1:8080/main", httpAccount.getURL());
        Assert.assertEquals("HTTPAccount: attribute login", "foo2", httpAccount.getLogin());
        Assert.assertEquals("HTTPAccount: attribute password", "bar2", httpAccount.getPassword());
        Assert.assertEquals("HTTPAccount: attribute through", "1", httpAccount.params.get("through"));
    }
}
