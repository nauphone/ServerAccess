/*
 * Copyright (C) 2016 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */

package ru.naumen.servacc.backend;

import ru.naumen.servacc.GlobalThroughView;
import ru.naumen.servacc.backend.DualChannel;
import ru.naumen.servacc.config2.Account;
import ru.naumen.servacc.config2.HTTPAccount;
import ru.naumen.servacc.config2.SSHAccount;
import ru.naumen.servacc.config2.i.IConfig;

/**
 * @author Andrey Hitrin
 * @since 29.01.16
 */
public interface Backend {
    void openSSHAccount(SSHAccount account, String path) throws Exception;

    void openHTTPAccount(HTTPAccount account) throws Exception;

    void localPortForward(SSHAccount account, String localHost, int localPort, String remoteHost, int remotePort) throws Exception;

    void browseViaFTP(SSHAccount account) throws Exception;

    DualChannel openProxyConnection(String host, int port, SSHAccount account) throws Exception;

    SSHAccount getThrough(Account account);

    void cleanup();

    void setGlobalThrough(SSHAccount account);

    void setGlobalThroughView(GlobalThroughView view);

    void selectNewGlobalThrough(String uniqueIdentity, IConfig config);

    void refresh(IConfig newConfig);

    void clearGlobalThrough();
}
