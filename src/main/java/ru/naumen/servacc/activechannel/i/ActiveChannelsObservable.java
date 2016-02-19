/**
 * 
 */
package ru.naumen.servacc.activechannel.i;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public interface ActiveChannelsObservable
{
    void addActiveChannelsObserver(ActiveChannelsObserver observer);
    void removeActiveChannelsObserver(ActiveChannelsObserver observer);
    void notifyActiveChannelsObservers();
}
