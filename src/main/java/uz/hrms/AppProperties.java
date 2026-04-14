package uz.hrms;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
record JwtProperties(
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String secret
) {
}

@ConfigurationProperties(prefix = "app.bootstrap-admin")
record BootstrapAdminProperties(
        boolean enabled,
        String username,
        String password,
        String email
) {
}
