/**
 *
 */
package ru.naumen.servacc.activechannel.visitors;

import ru.naumen.servacc.activechannel.i.IActiveChannel;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public interface IActiveChannelVisitor
{
    void visit(IActiveChannel channel);
}
