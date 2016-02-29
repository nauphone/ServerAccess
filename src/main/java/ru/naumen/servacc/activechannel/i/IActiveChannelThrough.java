/**
 *
 */
package ru.naumen.servacc.activechannel.i;

import java.util.List;

/**
 * @author vtarasov
 * @since 16.02.16
 */
public interface IActiveChannelThrough extends IActiveChannel
{
    List<IActiveChannel> getChildren();

    void addChild(IActiveChannel child);
    void removeChild(IActiveChannel child);
}
