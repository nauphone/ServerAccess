package ru.naumen.servacc.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.config2.CompositeConfig;
import ru.naumen.servacc.config2.Config;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigLoader;
import ru.naumen.servacc.util.AppProperties;
import ru.naumen.servacc.util.StringEncrypter;
import ru.naumen.servacc.util.Util;

public class ConfigLoader implements IConfigLoader
{
    private Map<String, String[]> authCache = new HashMap<String, String[]>();
    private final UIController controller;
    private final Shell shell;

    public ConfigLoader(UIController controller, Shell shell)
    {
        this.controller = controller;
        this.shell = shell;
    }

    public IConfig loadConfig() throws Exception
    {
        Properties properties = AppProperties.getAppProperties();
        CompositeConfig compositeConfig = new CompositeConfig();
        String[] keys = properties.keySet().toArray(new String[] {});
        Arrays.sort(keys);
        for (String key : keys)
        {
            if (key.matches("source[0-9]*"))
            {
                try
                {
                    IConfig config = loadConfig((String) properties.get(key));
                    if (config != null)
                    {
                        compositeConfig.add(config);
                    }
                }
                catch (Exception e)
                {
                    controller.showAlert(e.getLocalizedMessage());
                }
            }
        }
        return compositeConfig;
    }

    private IConfig loadConfig(String source) throws Exception
    {
        if (source.startsWith("http://") || source.startsWith("https://"))
        {
            return loadConfigViaHTTP(source);
        }
        else if (source.startsWith("file://"))
        {
            File file = new File(source.substring("file://".length()));
            if (!file.exists())
            {
                throw new IOException("File '" + file.getAbsolutePath() + "' does not exist.");
            }
            return loadConfigFromFile(source);
        }
        else
        {
            throw new RuntimeException("Unknown source type: " + source);
        }
    }

    private IConfig loadConfigViaHTTP(String url) throws Exception
    {
        HTTPResource resource = new HTTPResource(url);
        String[] auth = authCache.get(url);
        if (auth != null && auth.length == 2)
        {
            resource.setAuthentication(auth[0], auth[1]);
        }
        while (true)
        {
            try
            {
                return new Config(resource.getInputStream());
            }
            catch (HTTPResource.NotAuthenticatedError e)
            {
                ResourceDialog dialog = new ResourceDialog(shell, true);
                dialog.setURL(url);
                if (dialog.show())
                {
                    String login = dialog.getFieldValue("Login");
                    String password = dialog.getFieldValue("Password");
                    resource.setAuthentication(login, password);
                    authCache.put(url, new String[] {login, password});
                }
                else
                {
                    return null;
                }
            }
            finally
            {
                resource.close();
            }
        }
    }

    private IConfig loadConfigFromFile(String source) throws Exception
    {
        File file = new File(source.substring("file://".length()));
        if (!file.exists())
        {
            throw new IOException("File '" + file.getAbsolutePath() + "' does not exist.");
        }

        InputStream input = new FileInputStream(file);
        if (Util.isConfigEncrypted(source.substring("file://".length())))
        {
            input.skip(Util.header.length);
            String content = new Scanner(input).useDelimiter("\\A").next();
            String password = null;
            while (true)
            {
                try
                {
                    ResourceDialog dialog = new ResourceDialog(shell, false);
                    dialog.setURL(source);
                    if (dialog.show())
                    {
                        password = dialog.getFieldValue("Password");
                    }
                    else
                    {
                        return null;
                    }
                    content = new StringEncrypter("DESede", password).decrypt(content);
                    break;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            input = new ByteArrayInputStream(content.getBytes());
        }

        return new Config(input);
    }
}
