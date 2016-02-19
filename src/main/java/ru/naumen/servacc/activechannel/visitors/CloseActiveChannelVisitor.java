/**
 * 
 */
package ru.naumen.servacc.activechannel.visitors;

import ru.naumen.servacc.activechannel.i.IActiveChannel;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class CloseActiveChannelVisitor implements IActiveChannelVisitor
{
    @Override
    public void visit(IActiveChannel channel)
    {
        channel.close();
    }
}
