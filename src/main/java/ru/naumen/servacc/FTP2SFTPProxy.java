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

import com.mindbright.net.ftp.FTPException;
import com.mindbright.net.ftp.FTPServer;
import com.mindbright.net.ftp.FTPServerEventHandler;
import com.mindbright.ssh2.SSH2Connection;
import com.mindbright.ssh2.SSH2SFTP;
import com.mindbright.ssh2.SSH2SFTPClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Implements a proxy which proxies between an ftp client and an sftp server.
 */
public class FTP2SFTPProxy implements FTPServerEventHandler
{
    // FTP error codes
    public static final int CANNOT_OPEN_CONNECTION = 425;
    public static final int BAD_SEQUENCE_OF_COMMANDS = 503;
    public static final int FILE_UNAVAILABLE = 550;
    public static final int FILE_NAME_NOT_ALLOWED = 553;

    private static final Logger LOG = LoggerFactory.getLogger(FTP2SFTPProxy.class);

    private SSH2Connection connection;
    private SSH2SFTPClient sftp;
    private FTPServer ftp;
    private String remoteDir;
    private String renameFrom;
    private String user;
    private SSH2SFTP.FileAttributes attrs;

    public FTP2SFTPProxy(SSH2Connection connection, InputStream ftpInput, OutputStream ftpOutput, String identity) throws SSH2SFTP.SFTPException
    {
        initSFTP(connection);
        initFTP(ftpInput, ftpOutput, identity, false);
    }

    /**
     * Connect this instance with an <code>SSH2Connection</code> which is
     * connected to the server we want to transfer files to/from.
     *
     * @param connection Established connection to the server.
     */
    private void initSFTP(SSH2Connection connection) throws SSH2SFTP.SFTPException
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
     * @param ftpInput     The ftp command input stream.
     * @param ftpOutput    The ftp command output stream.
     * @param identity     Username to log in as
     * @param needPassword Tells the instance if it should request a password or not from
     *                     the user. The actual password the user then gives is ignored.
     */
    private void initFTP(InputStream ftpInput, OutputStream ftpOutput, String identity, boolean needPassword)
    {
        this.ftp = new FTPServer(identity, this, ftpInput, ftpOutput, needPassword);
    }

    /**
     * Login to server. This is actually a null operation for this class since
     * the user is already authenticated as part of the SSH connection.
     *
     * @param user Username to login as.
     * @param pass Password.
     * @return Returns true if the login was successful.
     */
    @Override
    public boolean login(String user, String pass)
    {
        connection.getLog().notice("SSH2FTPOverSFTP", "user " + user + " login");
        try
        {
            attrs = sftp.realpath(".");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to login as '%s'", user), e);
            // !!! TODO, should disconnect ???
            return false;
        }
        remoteDir = "/";
        this.user = user;
        return true;
    }

    @Override
    public void quit()
    {
        connection.getLog().notice("SSH2FTPOverSFTP", "user " + user + " logout");
        sftp.terminate();
    }

    @Override
    public boolean isPlainFile(String file)
    {
        try
        {
            attrs = sftp.lstat(expandRemote(file));
            return attrs.isFile();
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to check file '%s", file), e);
            return false;
        }
    }

    @Override
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
                LOG.error(String.format("Failed to get realpath of directory '%s'", newDir), e);
                throw new FTPException(FILE_UNAVAILABLE, dir + ": No such directory.");
            }
            newDir = attrs.lname;
            try
            {
                SSH2SFTP.FileHandle f = sftp.opendir(newDir);
                sftp.close(f);
            }
            catch (SSH2SFTP.SFTPException e)
            {
                LOG.error(String.format("Failed to open directory '%s'", newDir), e);
                throw new FTPException(FILE_UNAVAILABLE, dir + ": Not a directory.");
            }
            remoteDir = newDir;
        }
    }

    @Override
    public void renameFrom(String from) throws FTPException
    {
        String fPath = "";
        try
        {
            fPath = expandRemote(from);
            attrs = sftp.lstat(fPath);
            renameFrom = fPath;
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to rename file '%s' into '%s'", from, fPath), e);
            throw new FTPException(FILE_UNAVAILABLE, from + ": No such file or directory.");
        }
    }

    @Override
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
                LOG.error(String.format("Failed to rename file '%s' into '%s'", renameFrom, to), e);
                throw new FTPException(FILE_UNAVAILABLE, "rename: Operation failed.");
            }
            finally
            {
                renameFrom = null;
            }
        }
        else
        {
            throw new FTPException(BAD_SEQUENCE_OF_COMMANDS, "Bad sequence of commands.");
        }
    }

    @Override
    public void delete(String file) throws FTPException
    {
        try
        {
            sftp.remove(expandRemote(file));
        }
        catch (SSH2SFTP.SFTPPermissionDeniedException e)
        {
            LOG.error(String.format("Failed to delete file '%s'. Access denied", file), e);
            throw new FTPException(FILE_UNAVAILABLE, "access denied");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to delete file '%s'. No such file", file), e);
            throw new FTPException(FILE_UNAVAILABLE, file + ": no such file.");
        }
    }

    @Override
    public void rmdir(String dir) throws FTPException
    {
        try
        {
            sftp.rmdir(expandRemote(dir));
        }
        catch (SSH2SFTP.SFTPPermissionDeniedException e)
        {
            LOG.error(String.format("Failed to delete directory '%s'. Permission denied", dir), e);
            throw new FTPException(FILE_UNAVAILABLE, "access denied");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to delete directory '%s'. No such directory", dir), e);
            throw new FTPException(FILE_UNAVAILABLE, dir + ": no such directory.");
        }
    }

    @Override
    public void mkdir(String dir) throws FTPException
    {
        try
        {
            sftp.mkdir(expandRemote(dir), new SSH2SFTP.FileAttributes());
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to create directory '%s'", dir), e);
            // TODO: should we throw new exception here?
        }
    }

    @Override
    public String pwd()
    {
        return remoteDir;
    }

    @Override
    public String system()
    {
        return "UNIX Type: L8";
    }

    @Override
    public long modTime(String file) throws FTPException
    {
        return (timeAndSize(file))[0];
    }

    @Override
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
                throw new FTPException(FILE_UNAVAILABLE, "SFTP server don't return time/size.");
            }
            ts[0] = attrs.mtime * 1000L;
            ts[1] = attrs.size;
            return ts;
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to get file attributes for '%s'. No such file or directory", file), e);
            throw new FTPException(FILE_UNAVAILABLE, file + ": No such file or directory.");
        }
    }

    @Override
    public void store(String file, InputStream data, boolean binary) throws FTPException
    {
        try
        {
            String expandedFile = expandRemote(file);
            SSH2SFTP.FileHandle handle = sftp.open(expandedFile, SSH2SFTP.SSH_FXF_WRITE | SSH2SFTP.SSH_FXF_TRUNC |
                SSH2SFTP.SSH_FXF_CREAT, new SSH2SFTP.FileAttributes());
            sftp.writeFully(handle, data);
        }
        catch (IOException e)
        {
            LOG.error(String.format("Failed to store file '%s'. Error writing to data connection", file), e);
            throw new FTPException(CANNOT_OPEN_CONNECTION, "Error writing to data connection: " + e.getMessage());
        }
        catch (SSH2SFTP.SFTPPermissionDeniedException e)
        {
            LOG.error(String.format("Failed to store file '%s'. Permission denied", file), e);
            throw new FTPException(FILE_NAME_NOT_ALLOWED, file + ": Permission denied.");
        }
        catch (SSH2SFTP.SFTPException e)
        {
            LOG.error(String.format("Failed to store file '%s'. Error in SFTP connection", file), e);
            throw new FTPException(FILE_UNAVAILABLE, file + ": Error in SFTP connection, " + e.getMessage());
        }
        finally
        {
            try
            {
                data.close();
            }
            catch (Exception e)
            {
                LOG.warn("Unexpected error while closing chanel", e);
            }
        }
    }

    @Override
    public void retrieve(String file, OutputStream data, boolean binary) throws FTPException
    {
        try
        {
            String expandedFile = expandRemote(file);
            SSH2SFTP.FileHandle handle = sftp.open(expandedFile, SSH2SFTP.SSH_FXF_READ, new SSH2SFTP.FileAttributes());
            sftp.readFully(handle, data);
        }
        catch (SSH2SFTP.SFTPNoSuchFileException e)
        {
            LOG.error(String.format("Failed to retrieve file '%s'. No such file or directory", file), e);
            throw new FTPException(FILE_UNAVAILABLE, file + ": No such file or directory.");
        }
        catch (SSH2SFTP.SFTPException | IOException e)
        {
            LOG.error(String.format("Failed to retrieve file '%s'. Error in SFTP connection", file), e);
            throw new FTPException(FILE_UNAVAILABLE, file + ": Error in SFTP connection, " + e.getMessage());
        }
        finally
        {
            try
            {
                data.close();
            }
            catch (Exception e)
            {
                LOG.warn("Unexpected error while closing chanel", e);
            }
        }
    }

    private static String rightJustify(String s, int width)
    {
        String res = s;
        while (res.length() < width)
        {
            res = " " + res;
        }
        return res;
    }

    @Override
    public void list(String path, OutputStream data) throws FTPException
    {
        try
        {
            SSH2SFTP.FileAttributes[] list = dirList(path);
            Date currentDate = new Date();
            for (SSH2SFTP.FileAttributes attributes : list)
            {
                if (".".equals(attributes.name) || "..".equals(attributes.name))
                {
                    continue;
                }
                StringBuilder str = new StringBuilder();
                str.append(attributes.permString());
                str.append("    1 ");
                str.append(rightJustify(Integer.toString(attributes.uid), 8));
                str.append(" ");
                str.append(rightJustify(Integer.toString(attributes.gid), 8));
                str.append(" ");
                str.append(rightJustify(Long.toString(attributes.size), 16));
                str.append(" ");
                Date mtimeDate = new Date((long)attributes.mtime*1000);
                SimpleDateFormat dateFormat;
                if (mtimeDate.getYear() == currentDate.getYear())
                    dateFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.ENGLISH);
                else
                    dateFormat = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
                str.append(dateFormat.format(mtimeDate));
                str.append(" ");
                str.append(attributes.name);
                String row = str.toString();
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
            LOG.error(String.format("Failed to list content of directory '%s'", path), e);
            throw new FTPException(CANNOT_OPEN_CONNECTION, "Error writing to data connection: " + e.getMessage());
        }
    }

    @Override
    public void nameList(String path, OutputStream data) throws FTPException
    {
        try
        {
            SSH2SFTP.FileAttributes[] list = dirList(path);
            for (SSH2SFTP.FileAttributes attributes : list)
            {
                if (".".equals(attributes.name) || "..".equals(attributes.name))
                {
                    continue;
                }
                String row = attributes.name + "\r\n";
                data.write(row.getBytes());
            }
        }
        catch (IOException e)
        {
            LOG.error(String.format("Failed to name list at '%s'", path), e);
            throw new FTPException(CANNOT_OPEN_CONNECTION, "Error writing to data connection: " + e.getMessage());
        }
    }

    private SSH2SFTP.FileAttributes[] dirList(String path) throws FTPException
    {
        SSH2SFTP.FileHandle handle = null;
        SSH2SFTP.FileAttributes[] list;

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
                    for (SSH2SFTP.FileAttributes attributes : list)
                    {
                        attributes.lname = attributes.toString(attributes.name);
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
            LOG.error(String.format("Failed to list '%s'. Not a directory", path), e);
            throw new FTPException(FILE_UNAVAILABLE, path + ": Not a directory.");
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
            {
                LOG.warn("Unexpected error while closing chanel", e);
            }
        }

        return list;
    }

    @Override
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

    @Override
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
            LOG.error(String.format("Failed to change mode bits of file '%s'", file), e);
        }
    }
}
