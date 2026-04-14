package uz.hrms.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import uz.hrms.other.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    String generateAccessToken(CurrentUser currentUser) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(currentUser.username())
                .claim("uid", currentUser.userId().toString())
                .claim("roles", currentUser.roles())
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plus(jwtProperties.accessTokenTtl())))
                .signWith(secretKey);
        if (currentUser.employeeId() == null) {
            return builder.compact();
        }
        builder.claim("eid", currentUser.employeeId().toString());
        return builder.compact();
    }

    UUID extractUserId(String token) {
        return UUID.fromString(parse(token).getPayload().get("uid", String.class));
    }

    long expiresInSeconds() {
        return jwtProperties.accessTokenTtl().toSeconds();
    }

    Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parser().verifyWith(secretKey).requireIssuer(jwtProperties.issuer()).build().parseSignedClaims(token);
    }
}