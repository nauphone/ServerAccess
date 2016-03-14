/**
 * 
 */
package ru.naumen.servacc.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.mindbright.jca.security.KeyPair;
import com.mindbright.ssh2.SSH2Exception;
import com.mindbright.ssh2.SSH2KeyPairFile;

import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.HTTPResource.NotAuthenticatedError;
import ru.naumen.servacc.config2.SSHKey;
import ru.naumen.servacc.util.Util;

/**
 * @author vtarasov
 * @since 09.03.16
 */
public class SSHKeyLoader 
{
    private static final String TEMPFILENAME_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    private static final String PREFIX_HTTP = "http://";
    private static final String PREFIX_HTTPS = "https://";
    
    private final Shell shell;
    private final File localKeyStore;
    private final File tempKeyStore;
    
    private final Map<String, String[]> authCache = new HashMap<>();
    
    public SSHKeyLoader(final Shell shell, final File localKeyStore, final File tempKeyStore)
    {
        this.shell = shell;
        this.localKeyStore = localKeyStore;
        this.tempKeyStore = tempKeyStore;
    }
    
    public KeyPair loadKeyPair(final SSHKey key) throws Exception
    {
        final String keyPath = key.path;
        if (isKeyRemote(keyPath))
        {
            String[] loginPassword = getLoginPassword(keyPath);
            if (loginPassword == null)
            {
                loginPassword = requestAuth(keyPath);
            }
            
            KeyPair keyPair = loadKeyPairFromHTTP(keyPath, loginPassword, key.password);
            return keyPair;
            
            /* We need to use this logic when HTTP server return 401 response code
             * 
             * String[] loginPassword = getLoginPassword(keyPath);
            try
            {
                KeyPair keyPair = loadKeyPairFromHTTP(keyPath, loginPassword, key.password);
                return keyPair;
            }
            catch (NotAuthenticatedError e)
            {
                loginPassword = requestAuth(keyPath);
                
                KeyPair keyPair = loadKeyPairFromHTTP(keyPath, loginPassword, key.password);
                return keyPair;
            }*/
        }
        else
        {
            File keyFile = new File(localKeyStore, key.path);
            return loadKeyPair(keyFile, key.password);
        }
    }
    
    private boolean isKeyRemote(final String keyPath)
    {
        if (keyPath.startsWith(PREFIX_HTTP) || keyPath.startsWith(PREFIX_HTTPS))
        {
            return true;
        }
        return false;
    }
    
    private String[] getLoginPassword(final String keyPath) throws LoginPasswordNotFoundException
    {
        String keyPathHostPort = getRemoteSSHKeyHostPort(keyPath);
        if (authCache.containsKey(keyPathHostPort))
        {
            return authCache.get(keyPathHostPort);
        }
        return null;
    }
    
    private String[] requestAuth(final String keyPath) throws LoginPasswordNotFoundException
    {
        AuthenticationDialogShowerThread authDialogShowerThread = new AuthenticationDialogShowerThread(shell, keyPath);
        Display.getDefault().syncExec(authDialogShowerThread);
        
        if (authDialogShowerThread.getLoginPassword() == null)
        {
            throw new LoginPasswordNotFoundException(keyPath);
        }
        return authDialogShowerThread.getLoginPassword();
    }
    
    private void saveLoginPassword(final String keyPath, final String[] loginPassword)
    {
        String keyPathHostPort = getRemoteSSHKeyHostPort(keyPath);
        authCache.put(keyPathHostPort, loginPassword);
    }
    
    private void removeLoginPassword(final String keyPath)
    {
        String keyPathHostPort = getRemoteSSHKeyHostPort(keyPath);
        if (authCache.containsKey(keyPathHostPort))
        {
            authCache.remove(keyPathHostPort);
        }
    }
    
    private String generateTempKeyFileName()
    {
        return Util.generateRandomString(TEMPFILENAME_ALPHABET, 6);
    }
    
    private KeyPair loadKeyPair(final File keyFile, String keyPassword) throws IOException, SSH2Exception
    {
        SSH2KeyPairFile ssh2KeyPairFile = new SSH2KeyPairFile();
        ssh2KeyPairFile.load(keyFile.getAbsolutePath(), keyPassword);
        return ssh2KeyPairFile.getKeyPair(); 
    }
    
    private KeyPair loadKeyPairFromHTTP(final String keyPath, final String[] resourceLoginPassword, final String keyPassword) throws Exception
    {
        try (HTTPResource resource = new HTTPResource(keyPath))
        {
            if (resourceLoginPassword != null)
            {
                resource.setAuthentication(resourceLoginPassword[0], resourceLoginPassword[1]);
            }
            
            InputStream resourceInputStream = resource.getInputStream();
            
            if (resourceLoginPassword != null)
            {
                saveLoginPassword(keyPath, resourceLoginPassword);
            }
            
            File tempFile = null;
            try
            {
                tempFile = createTempKeyFile(resourceInputStream);
                KeyPair keyPair = loadKeyPair(tempFile, keyPassword);
                return keyPair;
            }
            finally
            {
                if (tempFile != null && tempFile.exists())
                {
                    removeTempFile(tempFile);
                }
            }
        }
        catch (NotAuthenticatedError e)
        {
            removeLoginPassword(keyPath);
            throw e;
        }
    }
    
    private File createTempKeyFile(InputStream keyFileInputStream) throws IOException
    {
        File tempFile = new File(tempKeyStore, generateTempKeyFileName());
        tempFile.createNewFile();
        
        try (BufferedOutputStream tempFileOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile)))
        {
            int c;
            final int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            while ((c = keyFileInputStream.read(buffer)) != -1)
            {
                tempFileOutputStream.write(buffer, 0, c);
            }
            
            byte[] eof = "\n".getBytes();
            tempFileOutputStream.write(eof);
        }
        
        /*String keyFileString;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
        {
            int c;
            final int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            while ((c = keyFileInputStream.read(buffer)) != -1)
            {
                byteArrayOutputStream.write(buffer, 0, c);
            }
            byteArrayOutputStream.flush();
            keyFileString = byteArrayOutputStream.toString();
        }
        
        int bodyBeginTagIndex = keyFileString.indexOf("<body>");
        int bodyEndTagIndex = keyFileString.indexOf("</body>");
        
        if (bodyBeginTagIndex != -1 && bodyEndTagIndex != -1)
        {
            keyFileString = keyFileString.substring(bodyBeginTagIndex + "<body>".length(), bodyEndTagIndex);
        }
        
        try (BufferedWriter tempFileWriter = new BufferedWriter(new FileWriter(tempFile)))
        {
            tempFileWriter.write(keyFileString);
        }*/
        
        return tempFile;
    }
    
    private void removeTempFile(File tempFile)
    {
        tempFile.delete();
    }
    
    private String getRemoteSSHKeyHostPort(String keyPath)
    {
        keyPath = keyPath.replace(PREFIX_HTTP, "");
        keyPath = keyPath.replace(PREFIX_HTTPS, "");
        
        int queryIndex = keyPath.indexOf("/");
        if (queryIndex != -1)
        {
            keyPath = keyPath.substring(0, queryIndex);
        }
        
        return keyPath;
    }
    
    public static class LoginPasswordNotFoundException extends Exception
    {
        private static final long serialVersionUID = -8081955361450656059L;

        public LoginPasswordNotFoundException(String keyPath)
        {
            super("Login/password not found to key path: " + keyPath);
        }
    }
    
    private static class AuthenticationDialogShowerThread implements Runnable
    {
        private final Shell shell;
        private final String resuourceUrl;
        private String[] loginPassword;
        
        private AuthenticationDialogShowerThread(final Shell shell, final String resuourceUrl)
        {
            this.shell = shell;
            this.resuourceUrl = resuourceUrl;
        }

        @Override
        public void run()
        {
            AuthenticationDialog dialog = new AuthenticationDialog(shell);
            dialog.setURL(resuourceUrl);
            if (dialog.show())
            {
                String login = dialog.getLogin();
                String password = dialog.getPassword();
                
                loginPassword = new String[] { login, password };
            }
        }
        
        public String[] getLoginPassword()
        {
            return loginPassword;
        }
    }
}
