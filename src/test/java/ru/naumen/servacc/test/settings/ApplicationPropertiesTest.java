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
import static ru.naumen.servacc.test.settings.FileUtils.write;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.naumen.servacc.settings.ApplicationProperties;
import ru.naumen.servacc.settings.PropertiesFile;

/**
 * @author Andrey Hitrin
 * @since 23.08.12
 */
public class ApplicationPropertiesTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File propertiesFile;

    @Before
    public void createConfigFolder() throws IOException
    {
        File configFolder = folder.newFolder();
        propertiesFile = new File(configFolder, "file.properties");
    }

    @Test
    public void canReadPropertiesFile() throws IOException
    {
        write(propertiesFile, "key=value");
        ApplicationProperties applicationProperties = new ApplicationProperties(propertiesFile);
        PropertiesFile appProperties = applicationProperties.getAppProperties();

        assertThat(appProperties.containsKey("key"), is(true));
        assertThat(appProperties.get("key").toString(), is("value"));
    }
}
