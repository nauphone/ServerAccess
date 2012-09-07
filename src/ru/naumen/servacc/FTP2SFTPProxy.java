/******************************************************************************
 *
 * Copyright (c) 1999-2003 AppGate Network Security AB. All Rights Reserved.
 *
 * This file contains Original Code and/or Modifications of Original Code as
 * defined in and that are subject to the MindTerm Public Source License,
 * Version 2.0, (the 'License'). You may not use this file except in compliance
 * with the License.
 *
 * You should have received a copy of the MindTerm Public Source License
 * along with this software; see the file LICENSE.  If not, write to
 * AppGate Network Security AB, Otterhallegatan 2, SE-41118 Goteborg, SWEDEN
 *
 *****************************************************************************/

package ru.naumen.servacc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mindbright.net.ftp.FTPException;
import com.mindbright.net.ftp.FTPServer;
import com.mindbright.net.ftp.FTPServerEventHandler;
import com.mindbright.ssh2.SSH2Connection;
import com.mindbright.ssh2.SSH2SFTP;
import com.mindbright.ssh2.SSH2SFTPClient;

/**
 * Implements a proxy which proxies between an ftp client and an sftp server.
 */
public class FTP2SFTPProxy implements FTPServerEventHandler
{

    protected SSH2Connection connection;
    protected SSH2SFTPClient sftp;
    protected FTPServer ftp;
    private String remoteDir;
    private String renameFrom;
    private String user;
    private SSH2SFTP.FileAttributes attrs;

    protected FTP2SFTPProxy(InputStream ftpInput, OutputStream ftpOutput, String identity, boolean needPassword)
    {
        initFTP(ftpInput, ftpOutput, identity, needPassword);
    }

    public FTP2SFTPProxy(SSH2Connection connection, InputStream ftpInput, OutputStream ftpOutput, String identity) throws SSH2SFTP.SFTPException
    {
        initSFTP(connection);
        // sftp.opendir("/");
        initFTP(ftpInput, ftpOutput, identity, false);
    }

    /**
     * Connect this instance with an <code>SSH2Connection</code> which is
     * connected to the server we want to transfer files to/from.
     *
     * @param connection
     *            Established connection to the server.
     */
    protected void initSFTP(SSH2Connection connection) throws SSH2SFTP.SFTPException
    {
        this.connection = connection;
        this.attrs = null;
        try
        {
            this.sftp = new SSH2SFTPClient(connection, false);
        }
        catch (SSH2SFTP.SFTPException e)
        {
            if (ftp != null)
            {
                ftp.terminate();
            }
            throw e;
        }
    }

    /**
     * Initialize the FTP server portion of this class.
     *
     * @param ftpInput
     *            The ftp command input stream.
     * @param ftpOutput
     *            The ftp command output stream.
     * @param identity
     *            Username to log in as
     * @param needPassowrd
     *            Tells the instance if it should request a password or not from
     *            the user. The actual password the user then gives is ignored.
     */
    protected void initFTP(InputStream ftpInput, OutputStream ftpOutput, String identity, boolean needPassword)
    {
        this.ftp = new FTPServer(identity, this, ftpInput, ftpOutput, needPassword);
    }

    /**
     * Login to server. This is actually a null operation for this class since
     * the user is already authenticated as part of the SSH connection.
     *
     * @param user
     *            Username to login as.
     * @param pass
     *            Password.
     *
     * @return Returns true if the login was successful.
     */
    public boolean login(String user, String pass)
    {
        connection.getLog().notice("SSH2FTPOverSFTP", "user " + user + " login");
        try
        {
            attrs = sftp.realpath(".");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            // !!! TODO, should disconnect ???
            return false;
        }
        remoteDir = "/";
        // remoteDir = attrs.lname;
        this.user = user;
        return true;
    }

    public void quit()
    {
        connection.getLog().notice("SSH2FTPOverSFTP", "user " + user + " logout");
        sftp.terminate();
    }

    public boolean isPlainFile(String file)
    {
        try
        {
            file = expandRemote(file);
            attrs = sftp.lstat(file);
            return attrs.isFile();
        }
        catch (SSH2SFTP.SFTPException e)
        {
            return false;
        }
    }

    public void changeDirectory(String dir) throws FTPException
    {
        if (dir != null)
        {
            String newDir = expandRemote(dir);
            try
            {
                attrs = sftp.realpath(newDir);
            }
            catch (SSH2SFTP.SFTPException e)
            {
                throw new FTPException(550, dir + ": No such directory.");
            }
            newDir = attrs.lname;
            try
            {
                SSH2SFTP.FileHandle f = sftp.opendir(newDir);
                sftp.close(f);
            }
            catch (SSH2SFTP.SFTPException e)
            {
                throw new FTPException(550, dir + ": Not a directory.");
            }
            remoteDir = newDir;
        }
    }

    public void renameFrom(String from) throws FTPException
    {
        try
        {
            String fPath = expandRemote(from);
            attrs = sftp.lstat(fPath);
            renameFrom = fPath;
        }
        catch (SSH2SFTP.SFTPException e)
        {
            throw new FTPException(550, from + ": No such file or directory.");
        }
    }

    public void renameTo(String to) throws FTPException
    {
        if (renameFrom != null)
        {
            try
            {
                sftp.rename(renameFrom, expandRemote(to));
            }
            catch (SSH2SFTP.SFTPException e)
            {
                throw new FTPException(550, "rename: Operation failed.");
            }
            finally
            {
                renameFrom = null;
            }
        }
        else
        {
            throw new FTPException(503, "Bad sequence of commands.");
        }
    }

    public void delete(String file) throws FTPException
    {
        try
        {
            sftp.remove(expandRemote(file));
        }
        catch (SSH2SFTP.SFTPException e)
        {
            String msg = (e instanceof SSH2SFTP.SFTPPermissionDeniedException) ? "access denied." : file + ": no such file.";
            throw new FTPException(550, msg);
        }
    }

    public void rmdir(String dir) throws FTPException
    {
        try
        {
            sftp.rmdir(expandRemote(dir));
        }
        catch (SSH2SFTP.SFTPException e)
        {
            String msg = (e instanceof SSH2SFTP.SFTPPermissionDeniedException) ? "access denied." : dir + ": no such directory.";
            throw new FTPException(550, msg);
        }
    }

    public void mkdir(String dir) throws FTPException
    {
        try
        {
            sftp.mkdir(expandRemote(dir), new SSH2SFTP.FileAttributes());
        }
        catch (SSH2SFTP.SFTPException e)
        {

        }
    }

    public String pwd()
    {
        return remoteDir;
    }

    public String system()
    {
        return "UNIX Type: L8";
    }

    public long modTime(String file) throws FTPException
    {
        return (timeAndSize(file))[0];
    }

    public long size(String file) throws FTPException
    {
        return (timeAndSize(file))[1];
    }

    private long[] timeAndSize(String file) throws FTPException
    {
        try
        {
            long[] ts = new long[2];
            String fPath = expandRemote(file);
            attrs = sftp.lstat(fPath);
            if (!attrs.hasSize || !attrs.hasModTime)
            {
                throw new FTPException(550, "SFTP server don't return time/size.");
            }
            ts[0] = attrs.mtime * 1000L;
            ts[1] = attrs.size;
            return ts;
        }
        catch (SSH2SFTP.SFTPException e)
        {
            throw new FTPException(550, file + ": No such file or directory.");
        }
    }

    public void store(String file, InputStream data, boolean binary) throws FTPException
    {
        SSH2SFTP.FileHandle handle = null;
        try
        {
            file = expandRemote(file);
            handle = sftp.open(file, SSH2SFTP.SSH_FXF_WRITE | SSH2SFTP.SSH_FXF_TRUNC | SSH2SFTP.SSH_FXF_CREAT, new SSH2SFTP.FileAttributes());

            sftp.writeFully(handle, data);

        }
        catch (IOException e)
        {
            throw new FTPException(425, "Error writing to data connection: " + e.getMessage());
        }
        catch (SSH2SFTP.SFTPPermissionDeniedException e)
        {
            throw new FTPException(553, file + ": Permission denied.");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            throw new FTPException(550, file + ": Error in sftp connection, " + e.getMessage());
        }
        finally
        {
            try
            {
                data.close();
            }
            catch (Exception e)
            { /* don't care */
            }
        }
    }

    public void retrieve(String file, OutputStream data, boolean binary) throws FTPException
    {
        SSH2SFTP.FileHandle handle = null;
        try
        {
            String eFile = expandRemote(file);
            handle = sftp.open(eFile, SSH2SFTP.SSH_FXF_READ, new SSH2SFTP.FileAttributes());
            sftp.readFully(handle, data);

        }
        catch (SSH2SFTP.SFTPNoSuchFileException e)
        {
            throw new FTPException(550, file + ": No such file or directory.");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            throw new FTPException(550, file + ": Error in sftp connection, " + e.getMessage());
        }
        catch (IOException e)
        {
            throw new FTPException(550, file + ": Error in sftp connection, " + e.getMessage());
        }
        finally
        {
            try
            {
                data.close();
            }
            catch (Exception e)
            { /* don't care */
            }
        }
    }

    private static String rightJustify(String s, int width)
    {
        String res = s;
        while (res.length() < width)
            res = " " + res;
        return res;
    }

    public void list(String path, OutputStream data) throws FTPException
    {
        try
        {
            SSH2SFTP.FileAttributes[] list = dirList(path);
            for (int i = 0; i < list.length; i++)
            {
                if (".".equals(list[i].name) || "..".equals(list[i].name))
                {
                    continue;
                }
                String row = list[i].lname;
                {
                    StringBuffer str = new StringBuffer();
                    str.append(list[i].permString());
                    str.append("    1 ");
                    str.append(rightJustify(Integer.toString(list[i].uid), 8));
                    str.append(" ");
                    str.append(rightJustify(Integer.toString(list[i].gid), 8));
                    str.append(" ");
                    str.append(rightJustify(Long.toString(list[i].size), 16));
                    str.append(" ");
                    str.append(list[i].mtime);
                    str.append(" ");
                    str.append(list[i].name);
                    row = str.toString();
                }
                if (row.endsWith("/"))
                {
                    row = row.substring(0, row.length() - 1);
                }
                row += "\r\n";
                data.write(row.getBytes());
            }
        }
        catch (IOException e)
        {
            throw new FTPException(425, "Error writing to data connection: " + e.getMessage());
        }
    }

    public void nameList(String path, OutputStream data) throws FTPException
    {
        try
        {
            SSH2SFTP.FileAttributes[] list = dirList(path);
            for (int i = 0; i < list.length; i++)
            {
                if (".".equals(list[i].name) || "..".equals(list[i].name))
                {
                    continue;
                }
                String row = list[i].name + "\r\n";
                data.write(row.getBytes());
            }
        }
        catch (IOException e)
        {
            throw new FTPException(425, "Error writing to data connection: " + e.getMessage());
        }
    }

    private SSH2SFTP.FileAttributes[] dirList(String path) throws FTPException
    {
        SSH2SFTP.FileHandle handle = null;
        SSH2SFTP.FileAttributes[] list = new SSH2SFTP.FileAttributes[0];

        try
        {
            String fPath = expandRemote(path);
            attrs = sftp.lstat(fPath);
            if (attrs.isDirectory())
            {
                handle = sftp.opendir(fPath);
                list = sftp.readdir(handle);
                if (list != null)
                {
                    for (int i = 0; i < list.length; i++)
                    {
                        list[i].lname = list[i].toString(list[i].name);
                    }
                }
            }
            else
            {
                list = new SSH2SFTP.FileAttributes[1];
                list[0] = new SSH2SFTP.FileAttributes();
                list[0].name = path;
                list[0].lname = attrs.toString(path);
            }
        }
        catch (SSH2SFTP.SFTPException e)
        {
            throw new FTPException(550, path + ": Not a directory.");
        }
        finally
        {
            try
            {
                if (handle != null)
                {
                    sftp.close(handle);
                }
            }
            catch (Exception e)
            { /* don't care */
            }
        }

        return list;
    }

    public void abort()
    {
    }

    private String expandRemote(String name)
    {
        if (name == null || name.length() == 0)
        {
            return remoteDir;
        }
        if (name.charAt(0) != '/')
        {
            name = remoteDir + "/" + name;
        }
        return name;
    }

    public void chmod(int mod, String file) throws FTPException
    {
        try
        {
            SSH2SFTP.FileAttributes fa = new SSH2SFTP.FileAttributes();
            fa.permissions = mod;
            fa.hasPermissions = true;
            sftp.setstat(expandRemote(file), fa);
        }
        catch (SSH2SFTP.SFTPException e)
        {
        }
    }
}
