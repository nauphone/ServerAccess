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

import java.util.HashMap;

import org.junit.Test;
import ru.naumen.servacc.config2.HTTPAccount;

/**
 * @author Andrey Hitrin
 * @since 23.08.13
 */
public class HTTPAccountTest
{
    @Test
    public void emptyAccountIsRepresentedAsEmptyString()
    {
        HTTPAccount account = new HTTPAccount();
        assertThat(account.toString(), is(""));
    }

    @Test
    public void accountURLIsRepresentedInToString()
    {
        HTTPAccount account = new HTTPAccount();
        account.setParams(new HashMap<String, String>() {{
            put("url", "http://google.com");
        }});
        assertThat(account.toString(), is("http://google.com"));
    }

    @Test
    public void loginIsRepresentedInToString()
    {
        HTTPAccount account = new HTTPAccount();
        account.setParams(new HashMap<String, String>() {{
            put("login", "superuser");
        }});
        assertThat(account.toString(), is("superuser @ "));
    }

    @Test
    public void loginAndURLAreRepresentedInToString()
    {
        HTTPAccount account = new HTTPAccount();
        account.setParams(new HashMap<String, String>() {{
            put("login", "superuser");
            put("url", "google.com");
        }});
        assertThat(account.toString(), is("superuser @ google.com"));
    }

    @Test
    public void commentIsRepresentedInToString()
    {
        HTTPAccount account = new HTTPAccount();
        account.setParams(new HashMap<String, String>() {{
            put("login", "superuser");
            put("url", "google.com");
        }});
        account.setComment("example comment");
        assertThat(account.toString(), is("superuser @ google.com (example comment)"));
    }

    @Test
    public void emptyCommentIsNotRepresentedInToString()
    {
        HTTPAccount account = new HTTPAccount();
        account.setParams(new HashMap<String, String>() {{
            put("login", "superuser");
            put("url", "google.com");
        }});
        account.setComment("");
        assertThat(account.toString(), is("superuser @ google.com"));
    }

}
