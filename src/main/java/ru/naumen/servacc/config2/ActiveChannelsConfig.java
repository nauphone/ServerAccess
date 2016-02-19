/**
 * 
 */
package ru.naumen.servacc.config2;

import java.util.Arrays;
import java.util.List;

import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public class ActiveChannelsConfig implements IConfig
{
    @Override
    public List<IConfigItem> getChildren()
    {
        return Arrays.asList(ActiveChannelsRegistry.getInstance());
    }
}
