package com.inso.sila.config.properties;

import com.inso.sila.config.SecurityPropertiesConfig;
import org.springframework.stereotype.Component;

@Component
public class SecurityProperties {

    private final SecurityPropertiesConfig.Auth auth;
    private final SecurityPropertiesConfig.Jwt jwt;

    public SecurityProperties(SecurityPropertiesConfig.Auth auth, SecurityPropertiesConfig.Jwt jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    public String getAuthHeader() {
        return auth.getHeader();
    }

    public String getAuthTokenPrefix() {
        return auth.getPrefix();
    }

    public String getLoginUri() {
        return auth.getLoginUri();
    }

    public String getJwtSecret() {
        return jwt.getSecret();
    }

    public String getJwtType() {
        return jwt.getType();
    }

    public String getJwtIssuer() {
        return jwt.getIssuer();
    }

    public String getJwtAudience() {
        return jwt.getAudience();
    }

    public Long getJwtExpirationTime() {
        return jwt.getExpirationTime();
    }

}
