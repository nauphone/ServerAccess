package ru.naumen.servacc.backend.sshd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Map;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.common.util.security.SecurityUtils;
import ru.naumen.servacc.HTTPResource;
import ru.naumen.servacc.IAuthenticationParametersGetter;

/**
 * @author Arkaev Andrei
 * @since 23.12.2019
 */
public class SshdKeyLoader {

    private final IAuthenticationParametersGetter authParamsGetter;

    public SshdKeyLoader(IAuthenticationParametersGetter authParamsGetter) {
        this.authParamsGetter = authParamsGetter;
    }

    public KeyPair getKeyPair(Map<String, String> params) throws Exception {

        final String keyUrl;

        if (params.containsKey("rsaKey")) {
            keyUrl = params.get("rsaKey");
        } else if (params.containsKey("dsaKey")) {
            keyUrl = params.get("dsaKey");
        } else {
            return null;
        }

        authParamsGetter.setResourcePath(keyUrl);
        authParamsGetter.doGet();
        String privateKeyContent = loadKey(keyUrl, authParamsGetter.getLogin(), authParamsGetter.getPassword());
        Iterable<KeyPair> keyPairs = SecurityUtils.loadKeyPairIdentities(null, null, new ByteArrayInputStream(privateKeyContent.getBytes()), null);

        return keyPairs.iterator().next();
    }

    private String loadKey(String uri, String user, String password) throws Exception {
        HTTPResource res = new HTTPResource(uri);
        res.setAuthentication(user, password);
        try (InputStream is = res.getInputStream()) {
            byte[] data = IoUtils.toByteArray(is);
            return new String(data, StandardCharsets.UTF_8);
        }
    }
}
