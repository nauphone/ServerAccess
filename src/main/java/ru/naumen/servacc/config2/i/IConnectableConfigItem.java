/**
 * 
 */
package ru.naumen.servacc.config2.i;

/**
 * @author vtarasov
 * @since 22.01.2016
 */
public interface IConnectableConfigItem extends IConnectable, IConfigItem
{
    default String getConnectionProcessIconName()
    {
        return "/icons/connect.gif";
    }
}
