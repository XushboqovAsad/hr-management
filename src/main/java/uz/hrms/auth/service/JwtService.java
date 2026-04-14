package uz.hrms.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import uz.hrms.config.JwtProperties;
import uz.hrms.security.CurrentUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(CurrentUser currentUser) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(currentUser.getUsername())
                .claim("uid", currentUser.getUserId().toString())
                .claim("roles", currentUser.getRoles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.accessTokenTtl())))
                .signWith(secretKey);
        if (currentUser.getEmployeeId() != null) {
            builder.claim("eid", currentUser.getEmployeeId().toString());
        }
        return builder.compact();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getPayload().get("uid", String.class));
    }

    public long accessTokenExpiresInSeconds() {
        return jwtProperties.accessTokenTtl().toSeconds();
    }

    public Jws<Claims> parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token);
    }
}
