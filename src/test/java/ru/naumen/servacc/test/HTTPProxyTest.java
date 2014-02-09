/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import ru.naumen.servacc.HTTPProxy;

/**
 * @author Andrey Hitrin
 * @since 16.01.13
 */
public class HTTPProxyTest
{
    @Test
    public void validUrl() throws MalformedURLException
    {
        URL url = toURL("GET http://google.com HTTP/1.1");
        assertThat(url.getProtocol(), is("http"));
        assertThat(url.getHost(), is("google.com"));
        assertThat(url.getPort(), is(-1));
        assertThat(url.getPath(), is(""));
    }

    @Test
    public void validUrlWithPort() throws MalformedURLException
    {
        URL url = toURL("GET http://example.com:234 HTTP/1.1");
        assertThat(url.getPort(), is(234));
    }

    @Test
    public void validUrlWithPath() throws MalformedURLException
    {
        URL url = toURL("GET http://example.com/path/to/file HTTP/1.1");
        assertThat(url.getPath(), is("/path/to/file"));
    }

    @Test(expected = MalformedURLException.class)
    public void urlWithoutProtocolIsInvalid() throws MalformedURLException
    {
        toURL("GET 192.168.211.123:8080/path HTTP/1.1");
    }

    private URL toURL(String request) throws MalformedURLException
    {
        return new HTTPProxy.URLBuilder(request).toURL();
    }
}
