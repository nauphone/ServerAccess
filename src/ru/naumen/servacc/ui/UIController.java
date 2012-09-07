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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ru.naumen.servacc.Backend;
import ru.naumen.servacc.FileResource;
import ru.naumen.servacc.SocketUtils;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.Group;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.config2.i.IConnectable;
import ru.naumen.servacc.config2.i.IFTPBrowseable;
import ru.naumen.servacc.config2.i.IPortForwarder;
import ru.naumen.servacc.platform.Platform;
import ru.naumen.servacc.util.ApplicationProperties;
import ru.naumen.servacc.util.StringEncrypter;
import ru.naumen.servacc.util.Util;

public class UIController
{
    private final Shell shell;
    private final Platform platform;
    private final ApplicationProperties applicationProperties;

    private Clipboard clipboard;
    private Backend backend;
    private ConfigLoader configLoader;
    private IConfig config;

    private FilteredTree filteredTree;
    private ToolItem toolitemConnect;
    private ToolItem toolitemPortForwarding;
    private ToolItem toolitemFTP;
    private ToolItem toolitemCopy;
    private ToolItem toolitemReloadConfig;

    private Label globalThrough;
    private String globalThroughUniqueIdentity;
    private Button clearGlobalThrough;

    private TreeItemController root;
    private TreeItemController selection;

    private Timer refreshTimer;

    private static Map<ImageKey, Image> images = new HashMap<ImageKey, Image>();

    public UIController(Shell shell, Platform platform, Backend backend, ApplicationProperties applicationProperties )
    {
        this.shell = shell;
        this.platform = platform;
        this.applicationProperties = applicationProperties;
        this.clipboard = new Clipboard(shell.getDisplay());
        this.backend = backend;
        this.configLoader = new ConfigLoader(this, shell, applicationProperties );
        createToolBar();
        createFilteredTree();
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
        filteredTree.getFilter().setFocus();
    }

    public void reloadConfig()
    {
        try
        {
            config = configLoader.loadConfig();
            buildTree(config);
            updateTree(filteredTree.getFilter().getText());
            if (!Util.isEmptyOrNull(globalThroughUniqueIdentity))
            {
                selectGlobalThrough(globalThroughUniqueIdentity);
            }
            else
            {
                doClearGlobalThrough();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            showAlert(e.getLocalizedMessage());
        }
    }

    public static Image getImage(String name)
    {
        return getImage(name, 0);
    }

    public static Image getImage(String name, int index)
    {
        ImageKey key = new ImageKey(name, index);
        if (images.containsKey(key))
        {
            return images.get(key);
        }
        else
        {
            ImageLoader imageLoader = new ImageLoader();
            InputStream is = UIController.class.getResourceAsStream(name);
            ImageData[] data = imageLoader.load(is);
            Image image = new Image(Display.getCurrent(), data[index]);
            images.put(key, image);
            return image;
        }
    }

    public void showAlert(String text)
    {
        MessageBox mb = new MessageBox(shell, SWT.SHEET);
        mb.setMessage(text);
        mb.open();
    }

    protected void showAlertFromThread(final String message)
    {
        Display.getDefault().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                showAlert(message);
            }
        });
    }

    public void cleanup()
    {
        backend.cleanup();
    }

    private void createToolBar()
    {
        ToolBar toolbar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);

        toolitemConnect = new ToolItem(toolbar, SWT.PUSH);
        toolitemConnect.setText("Connect");
        toolitemConnect.setImage(getImage("/icons/lightning.png"));
        toolitemConnect.setEnabled(false);

        toolitemPortForwarding = new ToolItem(toolbar, SWT.PUSH);
        toolitemPortForwarding.setText("Port Forwarding");
        toolitemPortForwarding.setImage(getImage("/icons/arrow-curve.png"));
        toolitemPortForwarding.setEnabled(false);

        toolitemFTP = new ToolItem(toolbar, SWT.PUSH);
        toolitemFTP.setText("FTP");
        toolitemFTP.setImage(getImage("/icons/drive-network.png"));
        toolitemFTP.setEnabled(false);

        toolitemCopy = new ToolItem(toolbar, SWT.PUSH);
        toolitemCopy.setText("Copy Password");
        toolitemCopy.setImage(getImage("/icons/document-copy.png"));
        toolitemCopy.setEnabled(false);

        new ToolItem(toolbar, SWT.SEPARATOR);

        toolitemReloadConfig = new ToolItem(toolbar, SWT.PUSH);
        toolitemReloadConfig.setText("Reload Accounts");
        toolitemReloadConfig.setImage(getImage("/icons/arrow-circle-double.png"));

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

    private void createFilteredTree()
    {
        filteredTree = new FilteredTree(shell, platform, SWT.NONE);
        filteredTree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
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
        // Drag source
        DragSource ds = new DragSource(filteredTree.getTree(), DND.DROP_MOVE);
        ds.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        ds.addDragListener(new DragSourceAdapter()
        {
            public void dragSetData(DragSourceEvent event)
            {
                event.doit = false;

                TreeItem[] selection = filteredTree.getTree().getSelection();
                if (selection.length == 1)
                {
                    TreeItemController tic = getConfigTreeItem(selection[0]);
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
                doClearGlobalThrough();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
            }
        });
        doClearGlobalThrough();

        // Drop target
        DropTarget dt = new DropTarget(widget, DND.DROP_MOVE);
        dt.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        dt.addDropListener(new DropTargetAdapter()
        {
            public void drop(DropTargetEvent event)
            {
                if (event.data != null)
                {
                    globalThroughUniqueIdentity = (String) event.data;
                    selectGlobalThrough(globalThroughUniqueIdentity);
                }
            }
        });
    }

    // TODO: these methods need refactoring
    private boolean selectGlobalThrough(String uniqueIdentity)
    {
        return selectGlobalThrough(config, uniqueIdentity, "");
    }

    private boolean selectGlobalThrough(Object object, String uniqueIdentity, String prefix)
    {
        if (object instanceof SSHAccount)
        {
            SSHAccount account = (SSHAccount) object;
            if (uniqueIdentity.equals(account.getUniqueIdentity()))
            {
                final Color fg = shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
                globalThrough.setText(prefix + " > " + account);
                globalThrough.setForeground(fg);
                clearGlobalThrough.setVisible(true);
                backend.setGlobalThrough(account);
                return true;
            }
        }
        else if (object instanceof IConfig)
        {
            for (IConfigItem i : ((IConfig) object).getChildren())
            {
                if (selectGlobalThrough(i, uniqueIdentity, prefix))
                {
                    return true;
                }
            }
        }
        else if (object instanceof Group)
        {
            for (IConfigItem i : ((Group) object).getChildren())
            {
                String newPrefix = ((Group) object).getName();
                if (!Util.isEmptyOrNull(prefix))
                {
                    newPrefix = prefix + " > " + newPrefix;
                }
                if (selectGlobalThrough(i, uniqueIdentity, newPrefix))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void doClearGlobalThrough()
    {
        final Color gray = shell.getDisplay().getSystemColor(SWT.COLOR_GRAY);
        globalThrough.setText("(drop an account here)");
        globalThrough.setForeground(gray);
        clearGlobalThrough.setVisible(false);
        globalThroughUniqueIdentity = null;
    }

    private void createTrayItem()
    {
        TrayItem trayItem = new TrayItem(shell.getDisplay().getSystemTray(), SWT.NULL);
        trayItem.setImage(getImage("/prog.ico", 1));
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

    private void createEncryptMenuItem(final Menu menu) {
        final MenuItem itemEncrypt = new MenuItem(menu, SWT.NULL);
        itemEncrypt.setText("Encrypt Local Accounts");
        itemEncrypt.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                encryptLocalAccounts();
            }
        });
    }

    private void createDecryptMenuItem(final Menu menu) {
        final MenuItem itemDecrypt = new MenuItem(menu, SWT.NULL);
        itemDecrypt.setText("Decrypt Local Accounts");
        itemDecrypt.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                decryptLocalAccounts();
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
                this.backend.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            backend.openSSHAccount((SSHAccount) tic.getData());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            showAlertFromThread(e.getMessage());
                        }
                    }
                });
            }
            else if (tic.getData() instanceof HTTPAccount)
            {
                this.backend.execute(new Runnable()
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
                            e.printStackTrace();
                            showAlertFromThread(e.getMessage());
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
            ex.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
                showAlert(e.getMessage());
            }
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
                e.printStackTrace();
            }
        }
    }

    private void encryptLocalAccounts()
    {
        try
        {
            Collection<String> configSources = applicationProperties.getConfigSources();

            int encryptableFiles = 0;
            for (String config : configSources)
            {
                if (!config.startsWith(FileResource.uriPrefix) || FileResource.isConfigEncrypted(config))
                {
                    continue;
                }
                encryptableFiles++;

                EncryptDialog dialog = new EncryptDialog(shell);
                dialog.setURL(config);
                dialog.show();
                String password = dialog.getPassword();

                if (Util.isEmptyOrNull(password))
                {
                    continue;
                }

                String content = new Scanner(configLoader.getConfigStream(config, shell)).useDelimiter("\\A").next();
                byte[] encryptedContent = new StringEncrypter("DESede", password).encrypt(content).getBytes();

                OutputStream os = new FileOutputStream(config.substring(FileResource.uriPrefix.length()));
                os.write(FileResource.encryptedHeader);
                os.write(System.getProperty("line.separator").getBytes());
                os.write(encryptedContent);
                os.close();
            }

            if (encryptableFiles < 1)
            {
                showAlert("All accounts are already encrypted");
            }
        }
        catch (Exception e)
        {
            showAlert(e.getMessage());
        }
    }

    private void decryptLocalAccounts()
    {
        try
        {
            Collection<String> configSources = applicationProperties.getConfigSources();

            int decryptableFiles = 0;
            for (String config : configSources)
            {
                String filePath = config.substring(FileResource.uriPrefix.length());
                if (!config.startsWith(FileResource.uriPrefix) || !FileResource.isConfigEncrypted(config))
                {
                    continue;
                }

                decryptableFiles++;

                InputStream stream = configLoader.getConfigStream(config, shell);
                if (stream == null)
                {
                    continue;
                }
                String content = new Scanner(stream).useDelimiter("\\A").next();
                stream.close();
                FileOutputStream os = new FileOutputStream(filePath);
                os.write(content.getBytes());
                os.close();
            }

            if (decryptableFiles < 1)
            {
                showAlert("All accounts are already decrypted");
            }
        }
        catch (Exception e)
        {
            showAlert(e.getMessage());
        }
    }

    // Build tree structure data from config
    private void buildTree(IConfig config)
    {
        root = new TreeItemController(platform);
        for (IConfigItem item : config.getChildren())
        {
            buildBranch(root, item);
        }
    }

    private void buildBranch(TreeItemController parent, IConfigItem config)
    {
        TreeItemController newTreeItem = new TreeItemController(parent, platform);
        newTreeItem.setData(config);
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
        List<String> filters = new Vector<String>();
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
            Boolean matches = item.matches(filters);
            if (matches)
            {
                item.setVisibility(true);
                raiseVisibility(item);
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
        Image image = tic.getImage();
        if (image != null)
        {
            treeItem.setImage(image);
        }
        for (TreeItemController child : tic.getChildren())
        {
            if (child.isVisible())
            {
                createTreeItem(treeItem, child);
            }
        }
    }

    private void raiseVisibility(TreeItemController item)
    {
        TreeItemController parent = item.getParent();
        while (!(parent == null || (parent.isExpanded() && parent.isVisible())))
        {
            parent.setVisibility(true);
            parent.setExpanded(true);
            parent = parent.getParent();
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
        TreeItem[] selection = filteredTree.getTree().getSelection();
        if ((selection.length) == 1)
        {
            return selection[0];
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

    private final static class ImageKey implements Comparable<ImageKey>
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
            if (other instanceof ImageKey)
            {
                return compareTo((ImageKey) other) == 0;
            }
            return false;
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
