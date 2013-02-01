package ru.naumen.servacc.settings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ru.naumen.servacc.settings.SourceListProvider;

/**
 * @author Andrey Hitrin
 * @since 21.09.12
 *
 * TODO: use better name
 */
public class FileSourceListProvider implements SourceListProvider
{
    private final Properties properties;

    public FileSourceListProvider(Properties properties)
    {
        this.properties = properties;
    }

    @Override
    public Collection<String> list()
    {
        List<String> result = new ArrayList<String>();
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
