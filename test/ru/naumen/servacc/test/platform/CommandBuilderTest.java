/*
 * Copyright (C) 2005-2013 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.platform;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import ru.naumen.servacc.platform.CommandBuilder;

/**
 * @author Andrey Hitrin
 * @since 09.02.13
 */
public class CommandBuilderTest
{
    private CommandBuilder builder;
    private Map<String,String> options = new HashMap<String, String>();

    @Test
    public void commandStringMustBeSplitByDoubleWhitespace()
    {
        builder = new CommandBuilder("one  two  three four");
        assertThat(builder.build(0, options), is(listOf("one", "two", "three four")));
    }

    @Test
    public void hostTemplateMustBeReplacedWithIP()
    {
        builder = new CommandBuilder("ping  {host}");
        assertThat(builder.build(0, options), is(listOf("ping", "127.0.0.1")));
    }

    @Test
    public void portTemplateMustBeReplacedWithGivenNumber()
    {
        builder = new CommandBuilder("ssh  {host}:{port}");
        assertThat(builder.build(22, options), is(listOf("ssh", "127.0.0.1:22")));
    }

    @Test
    public void optionsTemplateMustBeIgnoredWhenNoOptionsWereGiven()
    {
        builder = new CommandBuilder("putty  {options}  8.8.8.8");
        assertThat(builder.build(0, options), is(listOf("putty", "8.8.8.8")));
    }

    @Test
    public void optionsTemplateMustBeReplacedWithGivenOptionsList()
    {
        options.put("putty_options", "-load utf8");
        builder = new CommandBuilder("putty  {options}  8.8.8.8");
        assertThat(builder.build(0, options), is(listOf("putty", "-load", "utf8", "8.8.8.8")));
    }

    @Test
    public void keyForOptionsListMustBeDefinedByTheFirstCommandInLine()
    {
        options.put("putty_options", "fail");
        options.put("ping_options", "-c2");
        builder = new CommandBuilder("ping  {options}  goo.gl");
        assertThat(builder.build(0, options), is(listOf("ping", "-c2", "goo.gl")));
    }

    @Test
    public void nameTemplateIsReplacedByNameParameter()
    {
        options.put("name", "hello");
        builder = new CommandBuilder("xterm  -T  {name}");
        assertThat(builder.build(0, options), is(listOf("xterm", "-T", "hello")));
    }

    @Test
    public void urlTemplateIsUsedToBuildBrowserCommand()
    {
        builder = new CommandBuilder("cmd  /C  start {url}");
        assertThat(builder.build("http://google.com"), is(listOf("cmd", "/C", "start http://google.com")));
    }

    @Test
    public void urlThatContainsSpecialSymbolsCanBeProcessed()
    {
        builder = new CommandBuilder("firefox  {url}");
        String url = "http://secret.com/#/user$/temp?a=b&x=y";
        assertThat(builder.build(url), is(listOf("firefox", url)));
    }

    private List<String> listOf(String... strings)
    {
        return Arrays.asList(strings);
    }
}
