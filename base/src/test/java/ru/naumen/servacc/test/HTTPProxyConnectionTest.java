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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import ru.naumen.servacc.backend.Backend;
import ru.naumen.servacc.HTTPProxy;
import ru.naumen.servacc.MessageListener;
import ru.naumen.servacc.backend.mindterm.MindtermBackend;
import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.platform.OS;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

/**
 * @author Andrey Hitrin
 * @since 08.05.13
 */
public class HTTPProxyConnectionTest
{
    private static final int PORT = 1984;
    private static final String GOOGLE = "http://google.com";
    private static final String REDDIT = "http://www.reddit.com";
    private static final String GOOGLE_CONTENT = "YouTube";
    private static final String REDDIT_CONTENT = "ALIEN Logo";

    private final HttpHost proxyForHttpClient = new HttpHost("127.0.0.1", PORT);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final SSHAccount account = new SSHAccount();
    private final Backend backend = new MindtermBackend(new OS(), executorService, new ActiveChannelsRegistry(), null);
    private final MessageListener messageListener = new MessageListener()
    {
        @Override
        public void notify(String text)
        {
        }
    };
    @Rule
    public final Timeout testTimeout = new Timeout(10, TimeUnit.SECONDS);

    // system under test
    private final HTTPProxy proxy = new HTTPProxy(backend, executorService, new ActiveChannelsRegistry());

    @Before
    public void startProxy()
    {
        prepareSSHAccount();
        proxy.start();
        proxy.setProxyOn(account, PORT, messageListener);
        localhostCredentialsShouldBeDefined();
    }

    private void prepareSSHAccount()
    {
        account.setParams(new HashMap<String, String>()
        {{
                put(Account.ACCOUNT_PARAM_ADDRESS, "127.0.0.1");
                put(Account.ACCOUNT_PARAM_LOGIN, "<input your account here>");
                put(Account.ACCOUNT_PARAM_PASSWORD, "<input your password here>");
        }});
    }

    @After
    public void stopProxy()
    {
        proxy.finish();
    }

    @Test
    public void httpClientCanTouchGoogleDirectly() throws IOException
    {
        HttpResponse response = httpGet(GOOGLE);
        responseShouldContain(response, GOOGLE_CONTENT);
    }

    @Test
    public void httpClientCanTouchRedditDirectly() throws IOException
    {
        HttpResponse response = httpGet(REDDIT);
        responseShouldContain(response, REDDIT_CONTENT);
    }

    @Test
    public void httpClientCanTouchGoogleViaHttpProxy() throws IOException
    {
        HttpResponse response = httpGet(GOOGLE, proxyForHttpClient);
        responseShouldContain(response, GOOGLE_CONTENT);
    }

    @Test
    public void httpClientCanTouchDifferentRemoteHostsViaHttpProxy() throws IOException
    {
        HttpResponse firstResponse = httpGet(GOOGLE, proxyForHttpClient);
        HttpResponse secondResponse = httpGet(REDDIT, proxyForHttpClient);

        responseShouldContain(firstResponse, GOOGLE_CONTENT);
        responseShouldContain(secondResponse, REDDIT_CONTENT);
    }

    private void responseShouldContain(HttpResponse firstResponse, String content) throws IOException
    {
        assertThat(firstResponse.getStatusLine().getStatusCode(), is(200));
        assertThat(contentOf(firstResponse), containsString(content));
    }

    private void localhostCredentialsShouldBeDefined()
    {
        assumeThat(account.getLogin(), not(is("<input your account here>")));
    }

    private HttpResponse httpGet(String uri) throws IOException
    {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(new HttpGet(uri));
    }

    private HttpResponse httpGet(String uri, HttpHost proxy) throws IOException
    {
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setRoutePlanner(routePlanner)
            .build();
        return httpClient.execute(new HttpGet(uri));
    }

    private String contentOf(HttpResponse response) throws IOException
    {
        InputStream contentStream = response.getEntity().getContent();
        String content = new Scanner(contentStream, "UTF-8").useDelimiter("\\A").next();
        EntityUtils.consume(response.getEntity());
        return content;
    }
}
