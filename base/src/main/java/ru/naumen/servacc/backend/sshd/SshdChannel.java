/*
 * Copyright (C) 2005-2015 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.backend.sshd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import ru.naumen.servacc.backend.DualChannel;

/**
 * @author Arkaev Andrei
 * @since 23.12.2019
 */
public class SshdChannel implements DualChannel {

    private final SshdSocketAddress addr;
    private final OutputStream output;
    private final InputStream input;

    public SshdChannel(SshdSocketAddress addr) throws IOException {
        this.addr = addr;

        Socket s = new Socket(addr.getHostName(), addr.getPort());
        output = s.getOutputStream();
        input = s.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return output;
    }

    @Override
    public InputStream getInputStream() {
        return input;
    }
}
