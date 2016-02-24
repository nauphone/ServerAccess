/**
 * 
 */
package ru.naumen.servacc.activechannel;

import java.util.ArrayList;
import java.util.List;

import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public abstract class ActiveChannelThrough extends ActiveChannel implements IActiveChannelThrough
{
    private List<IActiveChannel> children = new ArrayList<IActiveChannel>();
    
    public ActiveChannelThrough(IActiveChannelThrough parent, ActiveChannelsRegistry registry)
    {
        super(parent, registry);
    }

    @Override
    public List<IActiveChannel> getChildren()
    {
        return getChildrenCopy();
    }
    
    private List<IActiveChannel> getChildrenCopy()
    {
        return new ArrayList<IActiveChannel>(children);
    }

    @Override
    public void addChild(IActiveChannel child)
    {
        children.add(child);
    }

    @Override
    public void removeChild(IActiveChannel child)
    {
        children.remove(child);
    }

    @Override
    public void accept(IActiveChannelVisitor visitor)
    {
        for (IActiveChannel child : getChildrenCopy())
        {
            child.accept(visitor);
        }
        
        super.accept(visitor);
    }
}
