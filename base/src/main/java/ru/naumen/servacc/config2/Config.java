/*
 * Copyright (C) 2005-2012 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.config2;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ru.naumen.servacc.config2.i.IConfig;
import ru.naumen.servacc.config2.i.IConfigItem;
import ru.naumen.servacc.util.Util;

public class Config implements IConfig
{
    public static final String GROUP_NODE_NAME = "Group";
    public static final String GROUP_ATTR_NAME = "name";
    public static final String GROUP_ATTR_COMMENT = "comment";

    public static final String ACCOUNT_NODE_NAME = "Account";
    public static final String ACCOUNT_ATTR_ID = "id";
    public static final String ACCOUNT_ATTR_TYPE = "type";
    public static final String ACCOUNT_ATTR_NAME = "name";
    public static final String ACCOUNT_ATTR_COMMENT = "comment";
    public static final String ACCOUNT_PARAM_THROUGH = "through";

    public static final String PARAM_NODE_NAME = "Param";
    public static final String PARAM_ATTR_NAME = "name";
    public static final String PARAM_ATTR_VALUE = "value";

    private Group root;

    public Config(InputStream stream) throws Exception
    {
        root = new Parser(stream).parseConfig();
    }

    public List<IConfigItem> getChildren()
    {
        return root.getChildren();
    }

    private class Parser
    {
        private InputStream stream;
        private Group rootGroup;
        private Map<String, Account> accounts;
        private Map<Account, String> needsThrough;

        Parser(InputStream stream)
        {
            this.stream = stream;
        }

        public Group parseConfig() throws Exception
        {
            rootGroup = new Group();
            accounts = new HashMap<>();
            needsThrough = new HashMap<>();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(stream);
            Node rootNode = doc.getDocumentElement();
            recursiveParseGroup(rootGroup, rootNode);
            // update 'through' references
            for (Entry<Account, String> e : needsThrough.entrySet())
            {
                e.getKey().setThrough(accounts.get(e.getValue()));
            }
            return rootGroup;
        }

        private void recursiveParseGroup(Group group, Node rootNode)
        {
            NodeList nodes = rootNode.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                String name = node.getNodeName();
                if (node.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                if (GROUP_NODE_NAME.equals(name))
                {
                    Group subGroup = parseGroup(node);
                    group.getChildren().add(subGroup);
                    recursiveParseGroup(subGroup, node);
                }
                else if (ACCOUNT_NODE_NAME.equals(name))
                {
                    group.getChildren().add(parseAccount(node));
                }
            }
        }

        private Group parseGroup(Node node)
        {
            String name = getAttribute(node, GROUP_ATTR_NAME, "");
            String comment = getAttribute(node, GROUP_ATTR_COMMENT, "");
            return new Group(name, comment);
        }

        private Account parseAccount(Node node)
        {
            String id = getAttribute(node, ACCOUNT_ATTR_ID);
            String type = getAttribute(node, ACCOUNT_ATTR_TYPE, "");
            String name = getAttribute(node, ACCOUNT_ATTR_NAME, "");
            String comment = getAttribute(node, ACCOUNT_ATTR_COMMENT, "");
            HashMap<String, String> params = new HashMap<>();
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node subNode = nodes.item(i);
                if (subNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                if (PARAM_NODE_NAME.equals(subNode.getNodeName()))
                {
                    String pName = getAttribute(subNode, PARAM_ATTR_NAME);
                    String pValue = getAttribute(subNode, PARAM_ATTR_VALUE);
                    if (pName != null && pValue != null)
                    {
                        params.put(pName, pValue);
                    }
                }
            }
            Account account;
            if (SSHAccount.SSHACCOUNT_TYPE.equalsIgnoreCase(type))
            {
                account = new SSHAccount();
            }
            else if (HTTPAccount.HTTPACCOUNT_TYPE.equalsIgnoreCase(type))
            {
                account = new HTTPAccount();
            }
            else
            {
                account = new Account();
            }
            account.setId(id);
            account.setType(type);
            account.setName(name);
            account.setComment(comment);
            account.setParams(params);
            accounts.put(account.getId(), account);
            String through = account.getParams().get(ACCOUNT_PARAM_THROUGH);
            if (!Util.isEmptyOrNull(through))
            {
                needsThrough.put(account, through);
            }
            return account;
        }

        private String getAttribute(Node node, String name, String defValue)
        {
            Node attrNode = node.getAttributes().getNamedItem(name);
            if (attrNode != null)
            {
                return attrNode.getNodeValue();
            }
            return defValue;
        }

        private String getAttribute(Node node, String name)
        {
            return getAttribute(node, name, null);
        }
    }
}
