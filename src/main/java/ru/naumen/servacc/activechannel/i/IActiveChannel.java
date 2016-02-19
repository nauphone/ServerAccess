/**
 * 
 */
package ru.naumen.servacc.activechannel.i;

import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;
import ru.naumen.servacc.config2.i.IConfigItem;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public interface IActiveChannel extends IConfigItem
{
    IActiveChannelThrough getParent();
    
    String getId();
    String[] getPath();
    
    void close();
    
    void save();
    void delete();
    
    boolean isActive();
    
    void accept(IActiveChannelVisitor visitor);
}
