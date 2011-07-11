/*
 * Copyright (C) 2005-2011 NAUMEN. All rights reserved.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 2 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 */
package ru.naumen.servacc.telnet;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import com.mindbright.ssh2.SSH2SessionChannel;

public class ConsoleManager
{
    private static Logger logger = Logger.getLogger("ru.naumen.servacc");

    // Telnet protocol commands:
    public static final int ABORT = 238;    // Abort
    public static final int AO = 245;       // Abort Output
    public static final int AYT = 246;      // Are You There
    public static final int BREAK = 243;    // Break
    public static final int DM = 242;       // Data Mark
    public static final int DO = 253;       // Request to use option
    public static final int DONT = 254;     // Don't use option
    public static final int EC = 247;       // Erase Character
    public static final int EL = 248;       // Erase Line
    public static final int EOF = 236;      // End Of File
    public static final int EOR = 239;      // End Of Record
    public static final int GA = 249;       // Go Ahead
    public static final int IAC = 255;      // Interpret As Command
    public static final int IP = 244;       // Interrupt Process
    public static final int NOP = 241;      // No Operation
    public static final int SB = 250;       // Start subnegotiation
    public static final int SE = 240;       // End subnegotiation
    public static final int SUSP = 237;     // Suspend process
    public static final int SYNCH = 242;    // Synchronize
    public static final int WILL = 251;     // Agree to use option
    public static final int WONT = 252;     // Refuse to use option
    // Telnet protocol options:
    public static final int O_ECHO = 1;             // Echo
    public static final int O_SUPPRESS_GA = 3;      // Suppress Go Ahead
    public static final int O_WINDOW_SIZE_NEG = 31; // Window Size Negotiation

    class ConsoleManagerInputStream extends PushbackInputStream
    {
        private ConsoleManager manager;

        public ConsoleManagerInputStream(ConsoleManager manager) throws IOException
        {
            super(new LFInputStream(manager.client.getInputStream()), 1024);
            this.manager = manager;
        }

        private void processDO() throws IOException
        {
            int option = super.read();
            switch (option)
            {
            case O_ECHO:
                say(IAC, WILL, O_ECHO);
                logger.info("IAC DO ECHO");
                break;
            case O_SUPPRESS_GA:
                say(IAC, WILL, O_SUPPRESS_GA);
                logger.info("IAC DO SUPPRESS GO AHEAD");
                break;
            default:
                say(IAC, WONT, option);
                logger.warning("IAC DO UNKNOWN(" + option + ")");
                break;
            }
        }

        private void processIAC() throws IOException
        {
            int command = super.read();
            switch (command)
            {
            case WILL:
                processWILL();
                break;
            case DO:
                processDO();
                break;
            case SB:
                processSB();
                break;
            default:
                logger.warning("IAC UNKNOWN(" + command + ")");
                break;
            }
        }

        private void processSB() throws IOException
        {
            int command = super.read();
            int val = super.read();
            Vector lst = new Vector();
            while (val != IAC)
            {
                lst.add(new Integer(val));
                val = super.read();
            }
            super.read();
            Integer[] arr = (Integer[]) lst.toArray(new Integer[] {});
            StringBuffer buffer = new StringBuffer();
            switch (command)
            {
            case O_WINDOW_SIZE_NEG:
                session.sendWindowChange(
                    arr[2].intValue() * 256 + arr[3].intValue(),
                    arr[0].intValue() * 256 + arr[1].intValue());
                logger.info("IAC SB WINDOW SIZE NEG: " + buffer);
                break;
            default:
                logger.warning("IAC SB UNKNOWN(" + command + buffer + ")");
                break;
            }
        }

        private void processWILL() throws IOException
        {
            int command = super.read();
            switch (command)
            {
            case O_SUPPRESS_GA:
                say(IAC, DO, O_SUPPRESS_GA);
                logger.info("IAC WILL SUPPRESS GO AHEAD");
                break;
            case O_WINDOW_SIZE_NEG:
                say(IAC, DO, O_WINDOW_SIZE_NEG);
                logger.info("IAC WILL WINDOW SIZE NEG");
                break;
            default:
                say(IAC, DONT, command);
                logger.warning("IAC WILL UNKNOWN(" + command + ")");
                break;
            }
        }

        public int read() throws IOException
        {
            int val = super.read();
            while (val == IAC || val == 17)
            {
                switch (val)
                {
                case IAC:
                    processIAC();
                    break;
                case 17:
                    unread(((String) params.get("password") + "\n").getBytes());
                    break;
                }
                val = super.read();
            }
            return val;
        }

        public int read(byte[] b, int off, int len) throws IOException
        {
            if (manager.inSudoLogin)
            {
                return 0;
            }
            while (true)
            {
                int res;
                if (available() != 0)
                {
                    res = super.read(b, off, available());
                }
                else
                {
                    res = super.read(b, off, len);
                }
                System.out.println(res);
                if (res == -1)
                {
                    return -1;
                }
                int ptr = 0;
                while (b[off + ptr] != (byte) IAC && b[off + ptr] != 17 && ptr < res)
                {
                    ptr++;
                }
                if (ptr == res)
                {
                    return res;
                }
                if (ptr != 0)
                {
                    unread(b, off + ptr, res - ptr);
                    return ptr;
                }
                unread(b, off, res);
                int nextb = read();
                if (nextb == -1)
                {
                    return -1;
                }
                unread(nextb);
            }
        }
    }

    class ConsoleManagerOutputStream extends FilterOutputStream
    {
        private ConsoleManager manager;
        public ConsoleManagerOutputStream(ConsoleManager manager) throws IOException
        {
            super(manager.client.getOutputStream());
            this.manager = manager;
        }

        public void write_(byte[] bs) throws IOException
        {
            out.write(bs);
        }

        public void write(byte[] b, int off, int len) throws IOException
        {
            if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
            {
                throw new IndexOutOfBoundsException();
            }
            out.write(b, off, len);
            //TODO: auto password enter
//            if (unreadPassword)
//            {
//                manager.in.unread(((String) manager.params.get("password") + "\n").getBytes());
//                manager.inSudoLogin = false;
//                unreadPassword = false;
//            }
            if (manager.inSudoLogin && b[0] != 27 && b[1] != 97)
            {
                manager.in.unread("sudo su -\n".getBytes());
                manager.inSudoLogin = false;
            }
        }

        public void write(int b) throws IOException
        {
            out.write(b);
        }
    }

    private Socket client;
    private ConsoleManagerInputStream in = null;
    private ConsoleManagerOutputStream out = null;
    private SSH2SessionChannel session;
    private HashMap params;
    private boolean inSudoLogin;

    public ConsoleManager(Socket client, SSH2SessionChannel session, HashMap params)
    {
        this.client = client;
        this.session = session;
        this.params = params;
        this.inSudoLogin = params.containsKey("sudologin");
    }

    public InputStream getInputStream() throws IOException
    {
        if (in == null)
        {
            in = new ConsoleManagerInputStream(this);
        }
        return in;
    }

    public OutputStream getOutputStream() throws IOException
    {
        if (out == null)
        {
            out = new ConsoleManagerOutputStream(this);
        }
        return out;
    }

    private void say(int a, int b, int c) throws IOException
    {
        getOutputStream().write(new byte[] {(byte) a, (byte) b, (byte) c});
    }

    public void negotiateProtocolOptions() throws IOException
    {
        say(IAC, DO, O_WINDOW_SIZE_NEG);
        say(IAC, WILL, O_ECHO);
        say(IAC, WILL, O_SUPPRESS_GA);
    }
}
