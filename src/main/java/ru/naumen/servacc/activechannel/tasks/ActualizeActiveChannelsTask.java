/**
 *
 */
package ru.naumen.servacc.activechannel.tasks;

import ru.naumen.servacc.activechannel.ActiveChannelsRegistry;

/**
 * @author vtarasov
 * @since 18.02.16
 */
public class ActualizeActiveChannelsTask extends Thread
{
    private static final int ACTUALIZATION_CHECK_TYME = 10000;

    private ActiveChannelsRegistry registry;

    public ActualizeActiveChannelsTask(ActiveChannelsRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Thread.currentThread().sleep(ACTUALIZATION_CHECK_TYME);
            }
            catch (InterruptedException e)
            {
                // Nothing to do
            }

            registry.actualizeAllChannels();
        }
    }
}
