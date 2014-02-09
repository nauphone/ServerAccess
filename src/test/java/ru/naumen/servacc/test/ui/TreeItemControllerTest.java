/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 */

package ru.naumen.servacc.test.ui;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.ui.TreeItemController;

/**
 * @author Andrey Hitrin
 * @since 23.12.12
 */
public class TreeItemControllerTest
{
    private IConfigItem data = new HTTPAccount();
    private TreeItemController controller = new TreeItemController();

    @Test
    public void dataMustBeSetInConstructor()
    {
        controller = new TreeItemController(null, data);
        assertThat(controller.getData(), is(data));
    }

    @Test
    public void defaultChildrenListIsEmpty()
    {
        assertThat(controller.getChildren().size(), is(0));
    }

    @Test
    public void visibleByDefault()
    {
        assertThat(controller.isVisible(), is(true));
    }

    @Test
    public void visibilityCanBeChanged()
    {
        controller.setVisibility(false);
        assertThat(controller.isVisible(), is(false));
        controller.setVisibility(true);
        assertThat(controller.isVisible(), is(true));
    }

    @Test
    public void collapsedByDefault()
    {
        assertThat(controller.isExpanded(), is(false));
    }

    @Test
    public void expandedStatusCanBeChanged()
    {
        controller.setExpanded(true);
        assertThat(controller.isExpanded(), is(true));
        controller.setExpanded(false);
        assertThat(controller.isExpanded(), is(false));
    }

    @Test
    public void doesNotMatchStringByDefault()
    {
        assertThat(controller.matches(Arrays.asList("some string")), is(false));
    }

    @Test
    public void matchesCollectionWhenDataAcceptsAllStrings()
    {
        IConfigItem matchingData = new IConfigItem()
        {
            @Override
            public boolean matches(String filter)
            {
                return true;
            }

            @Override
            public String getIconName()
            {
                return null;
            }
        };
        controller = new TreeItemController(null, matchingData);
        assertThat(controller.matches(Arrays.asList("one", "two", "three")), is(true));
    }

    @Test
    public void mayMatchWhenParentMatchesString()
    {
        IConfigItem matchingData = new IConfigItem()
        {
            @Override
            public boolean matches(String filter)
            {
                return "should match".equals(filter);
            }

            @Override
            public String getIconName()
            {
                return null;
            }
        };
        TreeItemController parent = new TreeItemController(null, matchingData);
        controller = new TreeItemController(parent, new HTTPAccount());
        assertThat(controller.matches(Arrays.asList("should match")), is(true));
    }

    @Test
    public void takesImageNameFromData()
    {
        IConfigItem data = new IConfigItem()
        {
            @Override
            public boolean matches(String filter)
            {
                return false;
            }

            @Override
            public String getIconName()
            {
                return "icon name";
            }
        };
        controller = new TreeItemController(null, data);
        assertThat(controller.getImageName(), is("icon name"));
    }

    @Test
    public void allParentsAreExpandedOnRaiseVisibility()
    {
        TreeItemController root = new TreeItemController(null, data);
        TreeItemController parent = new TreeItemController(root, data);
        controller = new TreeItemController(parent, data);

        root.setExpanded(false);
        parent.setExpanded(false);
        controller.raiseVisibility();
        assertThat(parent.isExpanded(), is(true));
        assertThat(root.isExpanded(), is(true));
    }

    @Test
    public void allParentsAreVisibleOnRaiseVisibility()
    {
        TreeItemController root = new TreeItemController(null, data);
        TreeItemController parent = new TreeItemController(root, data);
        controller = new TreeItemController(parent, data);

        root.setVisibility(false);
        parent.setVisibility(false);
        controller.raiseVisibility();
        assertThat(parent.isVisible(), is(true));
        assertThat(root.isVisible(), is(true));
    }

    @Test
    public void raiseVisibilityStopsOnTheFirstVisibleAndExpandedParent()
    {
        TreeItemController root = new TreeItemController(null, data);
        TreeItemController parent = new TreeItemController(root, data);
        controller = new TreeItemController(parent, data);

        root.setVisibility(false);
        parent.setVisibility(true);
        parent.setExpanded(true);
        controller.raiseVisibility();
        assertThat(root.isVisible(), is(false)); // process stops on parent
    }

    @Test
    public void defaultMessageIsEmpty()
    {
        assertThat(controller.toString(), is(""));
    }

    @Test
    public void messageIsTakenFromData()
    {
        Account account = new HTTPAccount();
        account.setParams(new HashMap<String, String>(){{
            put("url", "https://rvm.io");
        }});
        controller = new TreeItemController(null, account);
        assertThat(controller.toString(), is("https://rvm.io"));
    }
}
