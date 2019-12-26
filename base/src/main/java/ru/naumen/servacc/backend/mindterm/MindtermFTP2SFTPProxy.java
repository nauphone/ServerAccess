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

package ru.naumen.servacc.backend.mindterm;

import com.mindbright.net.ftp.FTPException;
import com.mindbright.net.ftp.FTPServer;
import com.mindbright.net.ftp.FTPServerEventHandler;
import com.mindbright.ssh2.SSH2SFTP.FileAttributes;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.apache.sshd.client.subsystem.sftp.SftpClient.Attribute;
import org.apache.sshd.client.subsystem.sftp.SftpClient.Attributes;
import org.apache.sshd.client.subsystem.sftp.SftpClient.CloseableHandle;
import org.apache.sshd.client.subsystem.sftp.SftpClient.DirEntry;
import org.apache.sshd.client.subsystem.sftp.SftpClient.OpenMode;
import org.apache.sshd.client.subsystem.sftp.SftpClientFactory;
import org.apache.sshd.client.subsystem.sftp.SftpRemotePathChannel;
import org.apache.sshd.common.util.io.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a proxy which proxies between an ftp client and an sftp server.
 */
public class MindtermFTP2SFTPProxy implements FTPServerEventHandler
{
    // FTP error codes
    public static final int CANNOT_OPEN_CONNECTION = 425;
    public static final int BAD_SEQUENCE_OF_COMMANDS = 503;
    public static final int FILE_UNAVAILABLE = 550;
    public static final int FILE_NAME_NOT_ALLOWED = 553;

    private static final Logger LOG = LoggerFactory.getLogger(MindtermFTP2SFTPProxy.class);

    private SftpClient sftp;
    private FTPServer ftp;
    private String remoteDir;
    private String renameFrom;
    private String user;
    private Attributes attrs;

    public MindtermFTP2SFTPProxy(ClientSession session, InputStream ftpInput, OutputStream ftpOutput, String identity) throws IOException
    {
        initSFTP(session);
        initFTP(ftpInput, ftpOutput, identity, false);
    }

    /**
     * Connect this instance with an <code>ClientSession</code> which is
     * connected to the server we want to transfer files to/from.
     *
     * @param session Established session to the server.
     */
    private void initSFTP(ClientSession session) throws IOException
    {
        this.attrs = null;
        try {
            this.sftp = SftpClientFactory.instance().createSftpClient(session);
        } catch (IOException e) {
            if (ftp != null) {
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
        //        session.getLog().notice("SSH2FTPOverSFTP", "user " + user + " login");
        try
        {
            attrs = sftp.stat(".");
        }
        catch (IOException e)
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
        //session.getLog().notice("SSH2FTPOverSFTP", "user " + user + " logout");
        try {
            sftp.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isPlainFile(String file)
    {
        try
        {
            attrs = sftp.lstat(expandRemote(file));
            return attrs.isRegularFile() || attrs.isSymbolicLink();
        }
        catch (IOException e)
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
                attrs = sftp.stat(newDir);
            }
            catch (IOException e)
            {
                LOG.error(String.format("Failed to get attrs of '%s'", newDir), e);
                throw new FTPException(FILE_UNAVAILABLE, dir + ": Failed to get attrs.");
            }

            if (!attrs.isDirectory()) {
                throw new FTPException(FILE_UNAVAILABLE, dir + ": Not a directory.");
            }

            try
            {
                CloseableHandle f = sftp.openDir(newDir);
                sftp.close(f);
            }
            catch (IOException e)
            {
                LOG.error(String.format("Failed to open directory '%s'", newDir), e);
                throw new FTPException(FILE_UNAVAILABLE, dir + ": Failed to open a directory.");
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
        catch (IOException e)
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
            catch (IOException e)
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
        catch (IOException e)
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
        catch (IOException e)
        {
            LOG.error(String.format("Failed to delete directory '%s'", dir), e);
            throw new FTPException(FILE_UNAVAILABLE, dir);
        }
    }

    @Override
    public void mkdir(String dir) throws FTPException
    {
        try
        {
            sftp.mkdir(expandRemote(dir));
        }
        catch (IOException e)
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
            Set<Attribute> flags = attrs.getFlags();
            if (!flags.contains(Attribute.Size) || !flags.contains(Attribute.ModifyTime))
            {
                throw new FTPException(FILE_UNAVAILABLE, "SFTP server don't return time/size.");
            }
            ts[0] = attrs.getModifyTime().toMillis();
            ts[1] = attrs.getSize();
            return ts;
        }
        catch (IOException e)
        {
            LOG.error(String.format("Failed to get file attributes for '%s'. No such file or directory", file), e);
            throw new FTPException(FILE_UNAVAILABLE, file + ": No such file or directory.");
        }
    }

    @Override
    public void store(String file, InputStream data, boolean binary) throws FTPException
    {
        String expandedFile = expandRemote(file);
        try {
            SftpRemotePathChannel channel = sftp.openRemoteFileChannel(expandedFile, OpenMode.Write, OpenMode.Truncate, OpenMode.Create);
            OutputStream outputStream = Channels.newOutputStream(channel);
            IoUtils.copy(data, outputStream);
        }
        catch (IOException e)
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
            SftpRemotePathChannel channel = sftp.openRemoteFileChannel(expandedFile, OpenMode.Read);
            InputStream inputStream = Channels.newInputStream(channel);
            IoUtils.copy(inputStream, data);
        }
        catch (IOException e)
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
    public void list(String path, OutputStream data) throws FTPException {
        path = expandRemote(path);

        try
        {
            Iterable<DirEntry> list = sftp.readDir(path);

            for (DirEntry entry : list)
            {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name)) {
                    continue;
                }

                Attributes attributes = entry.getAttributes();

                FileAttributes permAttrs = new FileAttributes();
                permAttrs.permissions = attributes.getPermissions();

                StringBuilder str = new StringBuilder();
                str.append(permAttrs.permString());
                str.append("    1 ");
                str.append(rightJustify(Integer.toString(attributes.getUserId()), 8));
                str.append(" ");
                str.append(rightJustify(Integer.toString(attributes.getGroupId()), 8));
                str.append(" ");
                str.append(rightJustify(Long.toString(attributes.getSize()), 16));
                str.append(" ");
                str.append(formatMtime(attributes.getModifyTime()));
                str.append(" ");
                str.append(name);
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
            Iterable<DirEntry> list = sftp.readDir(path);
            for (DirEntry dirEntry : list)
            {
                String name = dirEntry.getFilename();
                if (".".equals(name) || "..".equals(name))
                {
                    continue;
                }
                String row = name + "\r\n";
                data.write(row.getBytes());
            }
        }
        catch (IOException e)
        {
            LOG.error(String.format("Failed to name list at '%s'", path), e);
            throw new FTPException(CANNOT_OPEN_CONNECTION, "Error writing to data connection: " + e.getMessage());
        }
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
            Attributes fa = new Attributes();
            fa.setPermissions(mod);
            sftp.setStat(expandRemote(file), fa);
        }
        catch (IOException e)
        {
            LOG.error(String.format("Failed to change mode bits of file '%s'", file), e);
        }
    }

    private String formatMtime(FileTime mtime) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime mtimeDate = mtime.toInstant().atZone(ZoneId.systemDefault());
        DateTimeFormatter dateFormat;
        if (mtimeDate.getYear() == now.getYear()) {
            dateFormat = DateTimeFormatter.ofPattern("MMM dd HH:mm", Locale.ENGLISH);
        } else {
            dateFormat = DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
        }
        return mtimeDate.format(dateFormat);
    }

    private String permString(int permissions) {
        // TODO PosixFilePermissions.fromString()
        return "777";
    }
}
