/**
 * 
 */
package ru.naumen.servacc.activechannel.visitors;

import ru.naumen.servacc.activechannel.i.IActiveChannel;
import ru.naumen.servacc.activechannel.i.IHidableChannel;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class HideActiveChannelVisitor implements IActiveChannelVisitor
{
    @Override
    public void visit(IActiveChannel channel)
    {
        if (channel instanceof IHidableChannel)
        {
            ((IHidableChannel)channel).hide();
        }
    }
}
