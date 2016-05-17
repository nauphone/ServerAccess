/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.ui;

import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;

import ru.naumen.servacc.config2.i.IConnectableConfigItem;

/**
 * @author vtarasov
 * @since 22.01.2016
 */
public class WaitForConnectionTask implements Runnable
{
    private static final int MIN_DELAY = 100;

    private TreeItem treeItem;
    private IConnectableConfigItem configItem;
    private Future<?> connectionTaskFuture;

    public WaitForConnectionTask(TreeItem treeItem, IConnectableConfigItem configItem, Future<?> connectionTaskFuture)
    {
        this.treeItem = treeItem;
        this.configItem = configItem;
        this.connectionTaskFuture = connectionTaskFuture;
    }

    @Override
    public void run()
    {
        List<Image> images = ImageCache.getImages(configItem.getConnectionProcessIconName());

        int imageIndex = 0;
        while (imageIndex < images.size())
        {
            if (connectionTaskFuture.isDone())
            {
                changeTreeItemIcon(ImageCache.getImage(configItem.getIconName()));
                return;
            }

            Image image = images.get(imageIndex);
            int delay = image.getImageData().delayTime;

            changeTreeItemIcon(image);

            imageIndex++;
            if (imageIndex == images.size())
            {
                imageIndex = 0;
            }

            waitPause(delay);
        }
    }

    private void changeTreeItemIcon(Image image)
    {
        if (treeItem.isDisposed())
        {
            return;
        }

        treeItem.getDisplay().asyncExec(() ->
        {
            if (treeItem.isDisposed())
            {
                return;
            }

            treeItem.setImage(image);
            treeItem.getParent().update();
        });
    }

    private void waitPause(int delay)
    {
        try
        {
            Thread.currentThread().sleep(delay < MIN_DELAY ? MIN_DELAY : delay);
        }
        catch (InterruptedException e)
        {
        }
    }
}
