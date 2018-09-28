package ru.naumen.servacc.settings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import ru.naumen.servacc.settings.ListProvider;

/**
 * @author Andrey Hitrin
 * @since 21.09.12
 */
public class PropertiesFilter implements ListProvider
{
    private final Properties properties;
    private final String regex;

    public PropertiesFilter(Properties properties, String regex)
    {
        this.properties = properties;
        this.regex = regex;
    }

    @Override
    public Collection<String> list()
    {
        List<String> result = new ArrayList<>();
        String[] keys = properties.keySet().toArray(new String[properties.size()]);
        Arrays.sort(keys);
        for (String key : keys)
        {
            if (key.matches(regex))
            {
                result.add((String) properties.get(key));
            }
        }
        return result;
    }
}
