/*
 * Copyright (C) 2005-2015 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.backend.mindterm;

import com.mindbright.ssh2.SSH2InternalChannel;
import ru.naumen.servacc.backend.DualChannel;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * DualChannel adapter for MindTerm.
 * @author Andrey Hitrin
 * @since 14.10.15
 */
public class MindTermChannel implements DualChannel {

    private final SSH2InternalChannel channel;

    public MindTermChannel(SSH2InternalChannel adaptee) {
        channel = adaptee;
    }

    @Override
    public OutputStream getOutputStream() {
        return channel.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return channel.getInputStream();
    }
}
