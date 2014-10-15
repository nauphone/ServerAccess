package ru.naumen.servacc.config2;

/**
 * @author Andrey Hitrin
 * @since 17.10.14
 */
public class SSHKey
{
    public final String protocolType;
    public final String path;
    public final String password;

    public SSHKey(String protocolType, String path, String password)
    {
        this.protocolType = protocolType;
        this.path = path;
        this.password = password;
    }
}
