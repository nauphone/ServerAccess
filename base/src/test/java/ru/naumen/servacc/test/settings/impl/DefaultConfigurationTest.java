package ru.naumen.servacc.test.settings.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static ru.naumen.servacc.test.settings.FileUtils.contents;
import static ru.naumen.servacc.test.settings.FileUtils.write;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.naumen.servacc.settings.impl.DefaultConfiguration;

/**
 * @author Andrey Hitrin
 * @since 22.10.14
 */
public class DefaultConfigurationTest
{
    @Rule
    public TemporaryFolder root = new TemporaryFolder();

    @Test
    public void newConfigMustBeCreatedWhenItDoesNotExist() throws IOException
    {
        File userHomeDir = root.newFolder();
        File configFolder = new File(userHomeDir, ".serveraccess");
        DefaultConfiguration.create(configFolder);

        assertThat(configFolder.exists(), is(true));
        assertThat(Arrays.asList(configFolder.list()),
            containsInAnyOrder("accounts.xml", "serveraccess.properties", "window.properties"));
    }

    @Test
    public void defaultPropertiesFileMustContainALinkToDefaultAccountFile() throws IOException
    {
        File configFolder = root.newFolder();
        File serverAccessProperties = new File(configFolder, "serveraccess.properties");
        File accountsXML = new File(configFolder, "accounts.xml");
        String fileContents = "source=file://" + accountsXML.getAbsolutePath();

        DefaultConfiguration.create(configFolder);
        assertThat(contents(serverAccessProperties), hasItem(fileContents));
    }

    @Test
    public void existingPropertiesFileMustNotBeOverwritten() throws IOException
    {
        File configFolder = root.newFolder();
        File serverAccessProperties = new File(configFolder, "serveraccess.properties");
        String fileContents = "source1=https://noda.com/private/sa/config.xml";
        write(serverAccessProperties, fileContents);

        DefaultConfiguration.create(configFolder);
        assertThat(contents(serverAccessProperties), contains(fileContents));
    }

    @Test
    public void defaultAccountsFileMustBeEmpty() throws IOException
    {
        File configFolder = root.newFolder();
        File accountsXML = new File(configFolder, "accounts.xml");

        DefaultConfiguration.create(configFolder);
        assertThat(contents(accountsXML),
            contains("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>",
                    "<Accounts version=\"2\">",
                    "</Accounts>"));
    }

    @Test
    public void existingAccountsFileMustNotBeOverwritten() throws IOException
    {
        File configFolder = root.newFolder();
        File accountsXML = new File(configFolder, "accounts.xml");
        String fileContents = "<Accounts version=\"2\"><Group name\"Hello\"/></Accounts>";
        write(accountsXML, fileContents);

        DefaultConfiguration.create(configFolder);
        assertThat(contents(accountsXML), contains(fileContents));
    }
}
