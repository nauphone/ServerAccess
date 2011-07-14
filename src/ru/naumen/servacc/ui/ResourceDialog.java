/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.naumen.servacc.util.Util;

public class ResourceDialog extends DialogBase
{
    private Link link;
    private Map<String, Text> textFields = new HashMap<String, Text>();
    private Map<String, String> fieldValues = new HashMap<String, String>();

    private static class FieldDescriptor
    {
        public String name;
        public boolean isPassword;

        public FieldDescriptor(String name, boolean isPassword)
        {
            super();
            this.name = name;
            this.isPassword = isPassword;
        }
    };

    public static final FieldDescriptor[] defaultFields = { new FieldDescriptor("Login", false), new FieldDescriptor("Password", true) };
    public static final FieldDescriptor[] passwordFields = { new FieldDescriptor("Password", true) };
    public static final FieldDescriptor[] passwordChangeFields = { new FieldDescriptor("Password", true), new FieldDescriptor("Again", true) };

    public ResourceDialog(Shell parent, boolean loginNeeded)
    {
        this(parent, loginNeeded, "Your authentication is required to access the following resource.");
    }

    public ResourceDialog(Shell parent, FieldDescriptor[] fields, String descriptionText)
    {
        super(parent, 0);
        shell.setLayout(new GridLayout(2, false));

        GridData gridData;

        Label description = new Label(shell, SWT.WRAP);
        description.setText(descriptionText);
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gridData.horizontalSpan = 2;
        description.setLayoutData(gridData);

        createLabel("URL");
        link = new Link(shell, SWT.NONE);
        link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        link.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                if (!Util.isEmptyOrNull(e.text))
                {
                    Program.launch(e.text);
                }
            }
        });

        boolean focusExposed = false;
        for (FieldDescriptor field : fields)
        {
            createLabel(field.name);
            Text textField = new Text(shell, SWT.BORDER | (field.isPassword ? SWT.PASSWORD : 0));
            textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
            if (!focusExposed)
            {
                textField.setFocus();
                focusExposed = true;
            }
            textFields.put(field.name, textField);
            fieldValues.put(field.name, "");
        }

        Button button = new Button(shell, SWT.PUSH);
        button.setText("OK");
        gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gridData.horizontalSpan = 2;
        gridData.widthHint = 80;
        button.setLayoutData(gridData);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                close(true);
            }
        });
        shell.setDefaultButton(button);
        shell.pack();
        shell.setSize(Math.max(shell.getSize().x, 400), shell.getSize().y);
    }

    public ResourceDialog(Shell parent, boolean loginNeeded, String descriptionText)
    {
        this(parent, loginNeeded ? defaultFields : passwordFields, descriptionText);
    }

    public void setURL(String url)
    {
        if (!Util.isEmptyOrNull(url))
        {
            link.setText("<a href=\"" + url + "\">" + url + "</a>");
        }
    }

    public void setFieldValue(String name, String value)
    {
        if (!Util.isEmptyOrNull(value))
        {
            textFields.get(name).setText(value);
            fieldValues.put(name, value);
        }
    }

    public String getFieldValue(String name)
    {
        return fieldValues.get(name);
    }

    protected boolean validate()
    {
        for (String key : textFields.keySet())
        {
            fieldValues.put(key, textFields.get(key).getText());
        }
        return true;
    }
}
