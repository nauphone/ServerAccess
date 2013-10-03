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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import ru.naumen.servacc.Backend;
import ru.naumen.servacc.HTTPProxy;
import ru.naumen.servacc.MessageListener;
import ru.naumen.servacc.SocketUtils;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.Path;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.config2.i.IConnectable;
import ru.naumen.servacc.config2.i.IFTPBrowseable;
import ru.naumen.servacc.config2.i.IPortForwarder;
import ru.naumen.servacc.globalthrough.GlobalThroughController;
import ru.naumen.servacc.globalthrough.GlobalThroughView;
import ru.naumen.servacc.platform.Platform;
import ru.naumen.servacc.settings.ListProvider;
import ru.naumen.servacc.util.Util;

public class UIController implements GlobalThroughView
{
    private static final Logger LOGGER = Logger.getLogger(UIController.class);
    private final Shell shell;
    private final MessageListener synchronousAlert;
    private final MessageListener asynchronousAlert;

    private Clipboard clipboard;
    private Backend backend;
    private ExecutorService executor;
    private HTTPProxy httpProxy;
    private ConfigLoader configLoader;
    private IConfig config;

    private FilteredTree filteredTree;
    private ToolItem toolitemConnect;
    private ToolItem toolitemPortForwarding;
    private ToolItem toolitemFTP;
    private ToolItem toolItemProxy;
    private ToolItem toolitemCopy;
    private ToolItem toolitemReloadConfig;

    private Label globalThrough;
    private Button clearGlobalThrough;
    private GlobalThroughController globalThroughController;

    private TreeItemController root;
    private TreeItemController selection;

    private Timer refreshTimer;

    public UIController(Shell shell, Platform platform, Backend backend, ExecutorService executor, HTTPProxy httpProxy, ListProvider sourceListProvider)
    {
        this.shell = shell;
        this.clipboard = new Clipboard(shell.getDisplay());
        this.backend = backend;
        this.executor = executor;
        this.synchronousAlert = new SynchronousAlert(shell);
        this.asynchronousAlert = new AsynchronousProxy(synchronousAlert);
        this.configLoader = new ConfigLoader(shell, sourceListProvider, synchronousAlert);
        this.httpProxy = httpProxy;
        this.globalThroughController = new GlobalThroughController(this, backend);
        createToolBar();
        createFilteredTree(platform.useSystemSearchWidget());
        createGlobalThroughWidget();
        if (platform.isTraySupported())
        {
            createTrayItem();
        }
        else if (platform.isAppMenuSupported())
        {
            createAppMenu();
        }
        // set focus to search widget on startup
        filteredTree.focusOnFilterField();
    }

    public void reloadConfig()
    {
        try
        {
            config = configLoader.loadConfig();
            buildTree(config);
            updateTree(filteredTree.getFilter().getText());
            globalThroughController.refresh(config);
        }
        catch (Exception e)
        {
            LOGGER.error("Cannot reload config", e);
            showAlert(e.getLocalizedMessage());
        }
    }

    private void showAlert(String text)
    {
        synchronousAlert.notify(text);
    }

    private void showAlertAsync(String message)
    {
        asynchronousAlert.notify(message);
    }

    private void createToolBar()
    {
        ToolBar toolbar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);

        toolitemConnect = new ToolItem(toolbar, SWT.PUSH);
        toolitemConnect.setText("Connect");
        toolitemConnect.setImage(ImageCache.getImage("/icons/lightning.png"));
        toolitemConnect.setEnabled(false);

        toolitemPortForwarding = new ToolItem(toolbar, SWT.PUSH);
        toolitemPortForwarding.setText("Port Forwarding");
        toolitemPortForwarding.setImage(ImageCache.getImage("/icons/arrow-curve.png"));
        toolitemPortForwarding.setEnabled(false);

        toolitemFTP = new ToolItem(toolbar, SWT.PUSH);
        toolitemFTP.setText("FTP");
        toolitemFTP.setImage(ImageCache.getImage("/icons/drive-network.png"));
        toolitemFTP.setEnabled(false);

        toolItemProxy = new ToolItem(toolbar, SWT.PUSH);
        toolItemProxy.setText("HTTP Proxy");
        toolItemProxy.setImage(ImageCache.getImage("/icons/earth.png"));
        toolItemProxy.setEnabled(false);

        toolitemCopy = new ToolItem(toolbar, SWT.PUSH);
        toolitemCopy.setText("Copy Password");
        toolitemCopy.setImage(ImageCache.getImage("/icons/document-copy.png"));
        toolitemCopy.setEnabled(false);

        new ToolItem(toolbar, SWT.SEPARATOR);

        toolitemReloadConfig = new ToolItem(toolbar, SWT.PUSH);
        toolitemReloadConfig.setText("Reload Accounts");
        toolitemReloadConfig.setImage(ImageCache.getImage("/icons/arrow-circle-double.png"));

        toolbar.pack();

        // Events
        toolitemConnect.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                defaultActionRequested(getSelectedTreeItem());
            }
        });
        toolitemPortForwarding.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                portForwardingRequested(getSelectedTreeItem());
            }
        });
        toolitemFTP.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                ftpConnectionRequested(getSelectedTreeItem());
            }
        });
        toolItemProxy.addSelectionListener(new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent)
            {
                httpProxySetupRequested(getSelectedTreeItem());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent)
            {
            }
        });
        toolitemCopy.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                passwordCopyRequested(getSelectedTreeItem());
            }
        });
        toolitemReloadConfig.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                reloadConfig();
            }
        });
    }

    private void createFilteredTree(boolean useSystemSearchWidget)
    {
        filteredTree = new FilteredTree(shell, SWT.NONE, useSystemSearchWidget);
        // Selection handling
        filteredTree.getTree().addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                Object item = e.item;
                if (item == null)
                {
                    item = getSelectedTreeItem();
                }
                defaultActionRequested((TreeItem) item);
            }

            public void widgetSelected(SelectionEvent e)
            {
                TreeItem item = (TreeItem) e.item;
                if (item != null)
                {
                    itemSelected(item);
                    toolitemConnect.setEnabled(isConnectable(item));
                    toolitemPortForwarding.setEnabled(isPortForwarder(item));
                    toolitemFTP.setEnabled(isFTPBrowseable(item));
                    toolItemProxy.setEnabled(isPortForwarder(item));
                    toolitemCopy.setEnabled(isAccount(item));
                }
            }
        });
        // Filter events
        filteredTree.getFilter().addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                Text field = ((Text) e.getSource());
                filterTextChanged(field.getText());
            }
        });
        shell.getDisplay().addFilter(SWT.KeyDown, new Listener()
        {
            @Override
            public void handleEvent(Event event)
            {
                // Focus on Ctrl+C
                if (event.stateMask == SWT.CTRL && event.keyCode == (int) 'c')
                {
                    filteredTree.focusOnFilterField();
                }
            }
        });
        // Drag source
        DragSource ds = new DragSource(filteredTree.getTree(), DND.DROP_MOVE);
        ds.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        ds.addDragListener(new DragSourceAdapter()
        {
            public void dragSetData(DragSourceEvent event)
            {
                event.doit = false;

                TreeItem[] treeSelection = filteredTree.getTree().getSelection();
                if (treeSelection.length == 1)
                {
                    TreeItemController tic = getConfigTreeItem(treeSelection[0]);
                    if (tic.getData() instanceof SSHAccount)
                    {
                        event.data = ((SSHAccount) tic.getData()).getUniqueIdentity();
                        event.doit = true;
                    }
                }
            }
        });
    }

    private void createGlobalThroughWidget()
    {
        Composite widget = new Composite(shell, SWT.NONE);
        widget.setLayout(new GridLayout(3, false));
        widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        Label label = new Label(widget, SWT.NONE);
        label.setText("Connect all through:");
        globalThrough = new Label(widget, SWT.NONE);
        globalThrough.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        clearGlobalThrough = new Button(widget, SWT.NONE);
        clearGlobalThrough.setText("clear");
        clearGlobalThrough.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                globalThroughController.clear();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        globalThroughController.clear();

        // Drop target
        DropTarget dt = new DropTarget(widget, DND.DROP_MOVE);
        dt.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        dt.addDropListener(new DropTargetAdapter()
        {
            public void drop(DropTargetEvent event)
            {
                if (event.data != null)
                {
                    globalThroughController.select((String) event.data, config);
                }
            }
        });
    }

    @Override
    public void setGlobalThroughWidget(String globalThroughText)
    {
        final Color fg = shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        globalThrough.setText(globalThroughText);
        globalThrough.setForeground(fg);
        clearGlobalThrough.setVisible(true);
    }

    @Override
    public void clearGlobalThroughWidget()
    {
        final Color gray = shell.getDisplay().getSystemColor(SWT.COLOR_GRAY);
        globalThrough.setText("(drop an account here)");
        globalThrough.setForeground(gray);
        clearGlobalThrough.setVisible(false);
    }

    private void createTrayItem()
    {
        TrayItem trayItem = new TrayItem(shell.getDisplay().getSystemTray(), SWT.NULL);
        trayItem.setImage(ImageCache.getImage("/prog.ico", 1));
        trayItem.setVisible(true);
        // set up hide-to-tray behavior
        trayItem.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                shell.setVisible(true);
                shell.setFocus();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        shell.addShellListener(new ShellListener()
        {
            public void shellIconified(ShellEvent e)
            {
            }

            public void shellDeiconified(ShellEvent e)
            {
            }

            public void shellDeactivated(ShellEvent e)
            {
            }

            public void shellClosed(ShellEvent e)
            {
                e.doit = false;
                shell.setVisible(false);
            }

            public void shellActivated(ShellEvent e)
            {
            }
        });

        final Menu menu = new Menu(shell, SWT.POP_UP);
        // tray menu items to encrypt/decrypt local accounts
        createEncryptMenuItem(menu);
        createDecryptMenuItem(menu);
        new MenuItem(menu, SWT.SEPARATOR);
        // tray menu item which is the only way to quit on windows
        MenuItem itemQuit = new MenuItem(menu, SWT.NULL);
        itemQuit.setText("Quit");
        itemQuit.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event e)
            {
                shell.dispose();
            }
        });
        trayItem.addListener(SWT.MenuDetect, new Listener()
        {
            public void handleEvent(Event arg0)
            {
                menu.setVisible(true);
            }
        });
    }

    private void createAppMenu()
    {
        final Menu menuBar = Display.getCurrent().getMenuBar();
        if (menuBar != null)
        {
            final MenuItem file = new MenuItem(menuBar, SWT.CASCADE);
            final Menu dropdown = new Menu(menuBar);
            file.setText("File");
            file.setMenu(dropdown);
            createEncryptMenuItem(dropdown);
            createDecryptMenuItem(dropdown);
        }
    }

    private void createEncryptMenuItem(final Menu menu)
    {
        final MenuItem itemEncrypt = new MenuItem(menu, SWT.NULL);
        itemEncrypt.setText("Encrypt Local Accounts");
        itemEncrypt.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                configLoader.encryptLocalAccounts();
            }
        });
    }

    private void createDecryptMenuItem(final Menu menu)
    {
        final MenuItem itemDecrypt = new MenuItem(menu, SWT.NULL);
        itemDecrypt.setText("Decrypt Local Accounts");
        itemDecrypt.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                configLoader.decryptLocalAccounts();
            }
        });
    }

    // Event handlers
    private void filterTextChanged(String text)
    {
        scheduleRefresh(text);
    }

    private void itemSelected(TreeItem item)
    {
        selection = getConfigTreeItem(item);
    }

    private void defaultActionRequested(TreeItem item)
    {
        final TreeItemController tic = getConfigTreeItem(item);
        if (tic == null)
        {
            return;
        }
        try
        {
            if (tic.getData() instanceof SSHAccount)
            {
                this.executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            SSHAccount account = (SSHAccount) tic.getData();
                            Path path = Path.find(config, account.getUniqueIdentity());
                            backend.openSSHAccount(account, path.path());
                        }
                        catch (Exception e)
                        {
                            LOGGER.error("Cannot open SSH account", e);
                            showAlertAsync(e.getMessage());
                        }
                    }
                });
            }
            else if (tic.getData() instanceof HTTPAccount)
            {
                this.executor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            backend.openHTTPAccount((HTTPAccount) tic.getData());
                        }
                        catch (Exception e)
                        {
                            LOGGER.error("Cannot open HTTP account", e);
                            showAlertAsync(e.getMessage());
                        }
                    }
                });
            }
            else
            {
                throw new Exception("Unknown account type");
            }
        }
        catch (Exception ex)
        {
            LOGGER.error("Unexpected error", ex);
            showAlert(ex.getMessage());
        }
    }

    private void portForwardingRequested(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        if (tic != null && tic.getData() instanceof SSHAccount)
        {
            try
            {
                PortForwardingDialog dialog = new PortForwardingDialog(shell);
                dialog.setLocalPort(SocketUtils.getFreePort());
                dialog.setRemoteHost(SocketUtils.LOCALHOST);
                if (dialog.show())
                {
                    backend.localPortForward((SSHAccount) tic.getData(),
                        dialog.getLocalPort(),
                        dialog.getRemoteHost(),
                        dialog.getRemotePort());
                }
            }
            catch (Exception e)
            {
                LOGGER.error("Cannot forward port", e);
                showAlert(e.getMessage());
            }
        }
    }

    private void ftpConnectionRequested(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        if (tic != null && tic.getData() instanceof SSHAccount)
        {
            try
            {
                backend.browseViaFTP((SSHAccount) tic.getData());
            }
            catch (Exception e)
            {
                LOGGER.error("Cannot open FTP connection", e);
                showAlert(e.getMessage());
            }
        }
    }

    private void httpProxySetupRequested(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        if (tic != null && tic.getData() instanceof SSHAccount)
        {
            ProxySetupDialog dialog = new ProxySetupDialog(shell);
            dialog.show();

            httpProxy.setProxyOn((SSHAccount) tic.getData(), dialog.getPort(), asynchronousAlert);
            // TODO: display proxy status somewhere
        }
    }


    private void passwordCopyRequested(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        if (tic != null && tic.getData() instanceof Account)
        {
            try
            {
                final String password = ((Account) tic.getData()).getPassword();
                clipboard.setContents(
                    new Object[] {password},
                    new Transfer[] {TextTransfer.getInstance()});
            }
            catch (Exception e)
            {
                LOGGER.error("Cannot copy password", e);
            }
        }
    }

    // Build tree structure data from config
    private void buildTree(IConfig config)
    {
        root = new TreeItemController();
        for (IConfigItem item : config.getChildren())
        {
            buildBranch(root, item);
        }
    }

    private void buildBranch(TreeItemController parent, IConfigItem config)
    {
        TreeItemController newTreeItem = new TreeItemController(parent, config);
        if (config instanceof Group)
        {
            for (IConfigItem configItem : ((Group) config).getChildren())
            {
                buildBranch(newTreeItem, configItem);
            }
        }
        parent.getChildren().add(newTreeItem);
    }

    // Fill tree with data
    private void updateTree(String filter)
    {
        List<String> filters = new ArrayList<String>();
        if (!Util.isEmptyOrNull(filter))
        {
            for (String substr : filter.split(" "))
            {
                substr = substr.trim();
                if (substr.length() > 0)
                {
                    filters.add(substr);
                }
            }
        }
        updateBranch(root, filters);
        Tree tree = filteredTree.getTree();
        tree.setRedraw(false);
        tree.removeAll();
        for (TreeItemController item : root.getChildren())
        {
            if (item.isVisible())
            {
                createTreeItem(tree, item);
            }
        }
        // traverse tree and update expanded state
        updateExpandedState(tree);
        tree.setRedraw(true);
    }

    private void updateBranch(TreeItemController item, Collection<String> filters)
    {
        if (filters.size() > 0)
        {
            if (item.matches(filters))
            {
                item.setVisibility(true);
                item.raiseVisibility();
            }
            else
            {
                item.setVisibility(false);
            }
        }
        else
        {
            item.setVisibility(true);
            item.setExpanded(false);
        }
        for (TreeItemController child : item.getChildren())
        {
            updateBranch(child, filters);
        }
    }

    private void updateExpandedState(Tree tree)
    {
        for (TreeItem child : tree.getItems())
        {
            updateExpandedState(child);
        }
    }

    private void updateExpandedState(TreeItem item)
    {
        TreeItemController data = getConfigTreeItem(item);
        if (data == null || !data.isVisible())
        {
            return;
        }
        item.setExpanded(data.isExpanded());
        // update selection
        if (data == selection)
        {
            item.getParent().setSelection(item);
        }
        for (TreeItem child : item.getItems())
        {
            updateExpandedState(child);
        }
    }

    private void createTreeItem(Tree parent, TreeItemController tic)
    {
        TreeItem treeItem = new TreeItem(parent, SWT.NONE);
        setupTreeItem(treeItem, tic);
    }

    private void createTreeItem(TreeItem parent, TreeItemController tic)
    {
        TreeItem treeItem = new TreeItem(parent, SWT.NONE);
        setupTreeItem(treeItem, tic);
    }

    private void setupTreeItem(TreeItem treeItem, TreeItemController tic)
    {
        treeItem.setData(tic);
        treeItem.setText(tic.toString());
        String imageName = tic.getImageName();
        if (imageName != null)
        {
            treeItem.setImage(ImageCache.getImage(imageName));
        }
        for (TreeItemController child : tic.getChildren())
        {
            if (child.isVisible())
            {
                createTreeItem(treeItem, child);
            }
        }
    }

    private static boolean isPortForwarder(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        return (tic != null) && (tic.getData() instanceof IPortForwarder);
    }

    private static boolean isAccount(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        return (tic != null) && (tic.getData() instanceof Account);
    }

    private static boolean isConnectable(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        return (tic != null) && (tic.getData() instanceof IConnectable);
    }

    private static boolean isFTPBrowseable(TreeItem item)
    {
        TreeItemController tic = getConfigTreeItem(item);
        return (tic != null) && (tic.getData() instanceof IFTPBrowseable);
    }

    private static TreeItemController getConfigTreeItem(TreeItem item)
    {
        if (item != null)
        {
            Object data = item.getData();
            if (data instanceof TreeItemController)
            {
                return (TreeItemController) data;
            }
        }
        return null;
    }

    private TreeItem getSelectedTreeItem()
    {
        TreeItem[] treeSelection = filteredTree.getTree().getSelection();
        if ((treeSelection.length) == 1)
        {
            return treeSelection[0];
        }
        return null;
    }

    // Refresh tree
    private void scheduleRefresh(final String filter)
    {
        scheduleRefresh(filter, 300);
    }

    private void scheduleRefresh(final String filter, long delay)
    {
        if (refreshTimer != null)
        {
            refreshTimer.cancel();
        }
        TimerTask refreshTask = new TimerTask()
        {
            public void run()
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        updateTree(filter);
                        refreshTimer.cancel();
                    }
                });
            }
        };
        refreshTimer = new Timer();
        refreshTimer.schedule(refreshTask, delay);
    }
}
