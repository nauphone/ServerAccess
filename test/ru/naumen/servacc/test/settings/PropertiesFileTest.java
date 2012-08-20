/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.settings;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static ru.naumen.servacc.test.settings.FileUtils.contents;
import static ru.naumen.servacc.test.settings.FileUtils.write;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.naumen.servacc.settings.PropertiesFile;

/**
 * @author Andrey Hitrin
 * @since 04.09.12
 */
public class PropertiesFileTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File file;

    private PropertiesFile propertiesFile;

    @Before
    public void createFile() throws Exception
    {
        file = folder.newFile();
        propertiesFile = new PropertiesFile(file);
    }

    @Test
    public void canLoadFromXml() throws Exception
    {
        write(file, "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n" +
            "<properties>\n" +
            "<entry key=\"test\">123</entry>\n" +
            "</properties>");
        propertiesFile.load();
        assertThat(propertiesFile.keySet().size(), is(1));
        assertThat(propertiesFile.getProperty("test"), is("123"));
    }

    @Test
    public void canLoadFromProperties() throws Exception
    {
        write(file, "# comment here\n" +
            "key=456\n" +
            "passed = ok");
        propertiesFile.load();
        assertThat(propertiesFile.keySet().size(), is(2));
        assertThat(propertiesFile.getProperty("key"), is("456"));
        assertThat(propertiesFile.getProperty("passed"), is("ok"));
    }

    @Test
    public void writesDownAsXmlWhenLoadedAsXml() throws Exception
    {
        write(file, "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n<properties></properties>");
        propertiesFile.load();
        propertiesFile.setProperty("format", "XML");
        propertiesFile.store();

        List<String> contents = contents(file);
        assertThat(contents.size(), is(6));
        assertThat(contents.get(0), is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"));
        assertThat(contents.get(1), is("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"));
        assertThat(contents.get(2), is("<properties>"));
        assertThat(contents.get(3), is("<comment>This file is auto-generated. DO NOT EDIT IT MANUALLY</comment>"));
        assertThat(contents.get(4), is("<entry key=\"format\">XML</entry>"));
        assertThat(contents.get(5), is("</properties>"));
    }

    @Test
    public void writesDownAsPropertiesWhenLoadedAsProperties() throws Exception
    {
        write(file, "");
        propertiesFile.load();
        propertiesFile.setProperty("format", "plain");
        propertiesFile.store();

        List<String> contents = contents(file);
        assertThat(contents.size(), is(3));
        assertThat(contents.get(0), is("#This file is auto-generated. DO NOT EDIT IT MANUALLY"));
        assertThat(contents.get(1).startsWith("#"), is(true));      // timestamp
        assertThat(contents.get(2), is("format=plain"));
    }
}
