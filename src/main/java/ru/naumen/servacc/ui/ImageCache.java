/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

/**
 * Load any image once and store it in cache.
 * <p/>
 * Extracted from {@link UIController}
 *
 * @author Andrey Hitrin
 * @since 21.08.12
 */
public final class ImageCache
{
    private static final Logger LOGGER = Logger.getLogger(ImageCache.class);
    private static Map<ImageKey, Image> images = new HashMap<>();

    private ImageCache()
    {
        // Utility class should not have public constructor
    }
    
    public static boolean containsImage(String name)
    {
    	return containsImage(name, 0);
    }
    
    public static boolean containsImage(String name, int index)
    {
    	ImageKey key = new ImageKey(name, index);
    	return images.containsKey(key);
    }

    public static Image getImage(String name)
    {
        return getImage(name, 0);
    }

    public static Image getImage(String name, int index)
    {
        if (!containsImage(name, index))
        {
        	reloadImage(name);
        	if (!containsImage(name, index))
            {
        		return null;
            }
        }
        
        ImageKey key = new ImageKey(name, index);
        return images.get(key);
    }
    
    public static List<Image> getImages(String name) throws ArrayIndexOutOfBoundsException
    {
    	return getImages(name, 0);
    }
    
    public static List<Image> getImages(String name, int indexFrom) throws ArrayIndexOutOfBoundsException
    {
    	return getImages(name, indexFrom, Integer.MAX_VALUE);
    }
    
    public static List<Image> getImages(String name, int indexFrom, int indexTo) throws ArrayIndexOutOfBoundsException
    {
    	if (indexFrom < 0 || indexTo < 0)
    	{
    		throw new ArrayIndexOutOfBoundsException(String.format("indexFrom: %s, indexTo: %s", indexFrom, indexTo));
    	}
    	
    	List<Image> result = new ArrayList<Image>();
    	if (indexFrom > indexTo)
    	{
    		return result;
    	}
    	
    	if (!containsImage(name))
    	{
    		reloadImage(name);
    		if (!containsImage(name))
            {
    			return result;
            }
    	}
    	
    	for (int index = indexFrom; index < indexTo; index++)
    	{
    		if (!containsImage(name, index))
    		{
    			break;
    		}
    		
    		ImageKey key = new ImageKey(name, index);
			result.add(images.get(key));
    	}
    	
    	return result;
    }
    
    public int getImagePartsCount(String name)
    {
    	if (!containsImage(name))
    	{
    		reloadImage(name);
    		if (!containsImage(name))
            {
    			return 0;
            }
    	}
    	
    	int result = 0;

    	for (int index = 0; ; index++)
    	{
    		if (!containsImage(name, index))
    		{
    			break;
    		}
    		result++;
    	}
    	return result;
    }
    
    private static void reloadImage(String name)
    {
    	loadImage(name, true);
    }
    
    private static void loadImage(String name)
    {
    	loadImage(name, false);
    }
    
    private static void loadImage(String name, boolean reload)
    {
    	ImageLoader imageLoader = new ImageLoader();
        InputStream is = ImageCache.class.getResourceAsStream(name);
        if (is == null)
        {
            LOGGER.error("Cannot load image " + name);
            return;
        }
        
        if (!reload)
        {
        	ImageData[] data = imageLoader.load(is);
	        for (int index = 0; index < data.length; index++)
	        {
	        	ImageKey key = new ImageKey(name, index);
	        	if (!images.containsKey(key))
	        	{
	        		Image image = new Image(Display.getCurrent(), data[index]);
	        		images.put(key, image);
	        	}
	        }
        }
        else
        {
        	Map<ImageKey, Image> newImages = new HashMap<>();
        	
	        ImageData[] data = imageLoader.load(is);
	        for (int index = 0; index < data.length; index++)
	        {
	        	ImageKey key = new ImageKey(name, index);
	        	Image image = new Image(Display.getCurrent(), data[index]);
	        	newImages.put(key, image);
	        }
	        
	        for (int i = 0; ; i++)
	        {
	        	ImageKey eachKey = new ImageKey(name, i);
	        	if (!images.containsKey(eachKey))
	        	{
	        		break;
	        	}
	        	images.remove(eachKey);
	        }
	        
	        for (ImageKey key : newImages.keySet())
	        {
	        	images.put(key, newImages.get(key));
	        }
        }
    }

    private static final class ImageKey implements Comparable<ImageKey>
    {
        private final String name;
        private final int index;

        public ImageKey(String name, int index)
        {
            this.name = name;
            this.index = index;
        }

        public int compareTo(ImageKey other)
        {
            int result = name.compareTo(other.name);
            if (result == 0)
            {
                result = Integer.valueOf(index).compareTo(other.index);
            }
            return result;
        }

        public boolean equals(Object other)
        {
            return other instanceof ImageKey && compareTo((ImageKey) other) == 0;
        }

        public int hashCode()
        {
            return toString().hashCode();
        }

        public String toString()
        {
            return name + ", " + index;
        }
    }
}
