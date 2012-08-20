/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.settings.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static ru.naumen.servacc.test.settings.FileUtils.write;

import java.io.File;
import java.util.Collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.naumen.servacc.settings.ApplicationProperties;
import ru.naumen.servacc.settings.impl.FileSourceListProvider;

/**
 * @author Andrey Hitrin
 * @since 30.09.12
 */
public class FileSourceListProviderTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ApplicationProperties applicationProperties;
    private File configFolder;
    private File propertiesFile;
    private FileSourceListProvider sourceListProvider;

    @Before
    public void createConfigFolder() throws Exception
    {
        configFolder = folder.newFolder();
        propertiesFile = new File(configFolder, "file.properties");
    }

    @Test
    public void canListAllSourcesAtOnce() throws Exception
    {
        write(propertiesFile,
            "source=sourceFile\n" +
                "source1=anotherFile\n" +
                "hello=world");
        applicationProperties = new ApplicationProperties(propertiesFile);
        sourceListProvider = new FileSourceListProvider(applicationProperties);
        Collection<String> configSources = sourceListProvider.list();
        assertThat(configSources.size(), is(2));
        assertThat(configSources.contains("sourceFile"), is(true));
        assertThat(configSources.contains("anotherFile"), is(true));
    }

    @Test
    public void useOnlyLatestSourceOnCollision() throws Exception
    {
        write(propertiesFile,
            "source=missingSource\n" +
                "source=collision");
        applicationProperties = new ApplicationProperties(propertiesFile);
        sourceListProvider = new FileSourceListProvider(applicationProperties);
        Collection<String> configSources = sourceListProvider.list();
        assertThat(configSources.size(), is(1));
        assertThat(configSources.contains("missingSource"), is(false));
        assertThat(configSources.contains("collision"), is(true));
    }
}
