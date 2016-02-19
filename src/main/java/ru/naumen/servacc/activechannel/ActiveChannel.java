/**
 * 
 */
package ru.naumen.servacc.activechannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IActiveChannelThrough;
import ru.naumen.servacc.activechannel.visitors.IActiveChannelVisitor;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public abstract class ActiveChannel implements IActiveChannel
{
    private IActiveChannelThrough parent;
    
    public ActiveChannel(IActiveChannelThrough parent)
    {
        this.parent = parent;
    }

    @Override
    public IActiveChannelThrough getParent()
    {
        return parent;
    }

    @Override
    public String[] getPath()
    {
        List<String> path = new ArrayList<String>();
        
        if (parent != null)
        {
            path.addAll(Arrays.asList(parent.getPath()));
        }
        
        path.add(getId());
        
        return path.toArray(new String[path.size()]);
    }

    @Override
    public void close()
    {
        delete();
    }

    @Override
    public void save()
    {
        ActiveChannelsRegistry.getInstance().saveChannel(getPath(), this);
    }

    @Override
    public void delete()
    {
        ActiveChannelsRegistry.getInstance().deleteChannel(this);
    }

    @Override
    public void accept(IActiveChannelVisitor visitor)
    {
        visitor.visit(this);
    }
}
