package ru.naumen.servacc.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.widgets.Shell;

import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.config2.CompositeConfig;
import ru.naumen.servacc.config2.Config;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigLoader;
import ru.naumen.servacc.util.AppProperties;

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
            source = source.substring("file://".length());
            File file = new File(source);
            if (!file.exists())
            {
                throw new IOException("File '" + file.getAbsolutePath() + "' does not exist.");
            }
            return loadConfigFromFile(file);
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
                LoginDialog dialog = new LoginDialog(shell);
                dialog.setURL(url);
                if (dialog.show())
                {
                    String login = dialog.getLogin();
                    String password = dialog.getPassword();
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

    private IConfig loadConfigFromFile(File file) throws Exception
    {
        return new Config(new FileInputStream(file));
    }
}
