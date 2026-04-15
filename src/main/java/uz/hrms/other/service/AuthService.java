package uz.hrms.other.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUserService securityUserService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    AuthService(UserAccountRepository userAccountRepository,
                RefreshTokenRepository refreshTokenRepository,
                SecurityUserService securityUserService,
                JwtService jwtService,
                PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityUserService = securityUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    TokenResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndDeletedFalse(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.isActive() == false) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is inactive");
        }
        if (passwordEncoder.matches(request.password(), user.getPasswordHash()) == false) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        user.setLastLoginAt(Instant.now());
        CurrentUser currentUser = securityUserService.toCurrentUser(user);
        return issueTokens(user, currentUser, httpServletRequest);
    }

    TokenResponse refresh(RefreshRequest request, HttpServletRequest httpServletRequest) {
        String tokenHash = hash(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid"));
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevokedAt(Instant.now());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        refreshToken.setRevokedAt(Instant.now());
        CurrentUser currentUser = securityUserService.toCurrentUser(refreshToken.getUser());
        return issueTokens(refreshToken.getUser(), currentUser, httpServletRequest);
    }

    void logout(LogoutRequest request) {
        String tokenHash = hash(request.refreshToken());
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash).ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    CurrentUserResponse me(CurrentUser currentUser) {
        UserAccount user = userAccountRepository.findByIdAndDeletedFalse(currentUser.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        String fullName = String.join(" ", user.getLastName(), user.getFirstName(), user.getMiddleName() == null ? "" : user.getMiddleName()).trim();
        return new CurrentUserResponse(currentUser.userId(), currentUser.employeeId(), currentUser.username(), fullName, currentUser.roles(), currentUser.permissions());
    }

    private TokenResponse issueTokens(UserAccount user, CurrentUser currentUser, HttpServletRequest httpServletRequest) {
        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(30L * 24 * 60 * 60));
        refreshToken.setIpAddress(httpServletRequest.getRemoteAddr());
        refreshToken.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        refreshTokenRepository.save(refreshToken);
        return new TokenResponse(jwtService.generateAccessToken(currentUser), rawRefreshToken, jwtService.expiresInSeconds(), me(currentUser));
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
