package ru.naumen.servacc.settings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ru.naumen.servacc.settings.ApplicationProperties;
import ru.naumen.servacc.settings.SourceListProvider;

/**
 * @author Andrey Hitrin
 * @since 21.09.12
 */
public class FileSourceListProvider implements SourceListProvider
{
    private ApplicationProperties applicationProperties;

    public FileSourceListProvider(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Collection<String> list()
    {
        List<String> result = new ArrayList<String>();
        Properties properties = applicationProperties.getAppProperties();
        String[] keys = properties.keySet().toArray(new String[properties.size()]);
        Arrays.sort(keys);
        for (String key : keys)
        {
            if (key.matches("source[0-9]*"))
            {
                result.add((String) properties.get(key));
            }
        }
        return result;
    }
}
