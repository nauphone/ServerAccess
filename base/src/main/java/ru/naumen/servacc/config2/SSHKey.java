package ru.naumen.servacc.config2;

/**
 * @author Andrey Hitrin
 * @since 17.10.14
 */
public class SSHKey
{
    private final String protocolType;
    private final String path;
    private final String password;

    public SSHKey(String protocolType, String path, String password)
    {
        this.protocolType = protocolType;
        this.path = path;
        this.password = password;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }
}
