package co.com.crediya.jwttokenserviceadapter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private Keys keys = new Keys();
    private long expiration;

    public Keys getKeys() { return keys; }
    public void setKeys(Keys keys) { this.keys = keys; }
    public long getExpiration() { return expiration; }
    public void setExpiration(long expiration) { this.expiration = expiration; }

    public static class Keys {
        private String publicKey;

        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    }
}
