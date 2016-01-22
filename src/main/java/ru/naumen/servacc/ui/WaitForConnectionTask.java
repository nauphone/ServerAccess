/**
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
			if (treeItem.isDisposed())
			{
				return;
			}
			
			if (connectionTaskFuture.isDone())
			{
				treeItem.getDisplay().asyncExec(new Runnable()
                {
					@Override
					public void run()
					{
						treeItem.setImage(ImageCache.getImage(configItem.getIconName()));
						treeItem.getParent().update();
					}
                });
				
				return;
			}
			
			Image image = images.get(imageIndex);
			int delay = image.getImageData().delayTime;
			
			treeItem.getDisplay().asyncExec(new Runnable()
            {
				@Override
				public void run()
				{
					treeItem.setImage(image);
					treeItem.getParent().update();
				}
            });
			
			imageIndex++;
			if (imageIndex == images.size())
			{
				imageIndex = 0;
			}
			
			try
			{
				Thread.currentThread().sleep(delay < MIN_DELAY ? MIN_DELAY : delay);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
}
