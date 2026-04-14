package uz.hrms.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.audit.SecurityAuditService;
import uz.hrms.auth.entity.RefreshToken;
import uz.hrms.auth.entity.UserAccount;
import uz.hrms.auth.repository.RefreshTokenRepository;
import uz.hrms.auth.repository.UserAccountRepository;
import uz.hrms.auth.web.AuthDtos;
import uz.hrms.security.CurrentUser;

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
    private final SecurityAuditService securityAuditService;

    public AuthService(UserAccountRepository userAccountRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       SecurityUserService securityUserService,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       SecurityAuditService securityAuditService) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityUserService = securityUserService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.securityAuditService = securityAuditService;
    }

    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest request, HttpServletRequest httpServletRequest) {
        UserAccount user = userAccountRepository.findByUsernameIgnoreCaseAndIsDeletedFalse(request.username()).orElse(null);
        if (user == null) {
            securityAuditService.recordLoginEvent(null, request.username(), "LOGIN", "FAILURE", "INVALID_CREDENTIALS", httpServletRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (user.isActive() == false) {
            securityAuditService.recordLoginEvent(user.getId(), user.getUsername(), "LOGIN", "FAILURE", "INACTIVE_ACCOUNT", httpServletRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is inactive");
        }
        if (passwordEncoder.matches(request.password(), user.getPasswordHash()) == false) {
            securityAuditService.recordLoginEvent(user.getId(), user.getUsername(), "LOGIN", "FAILURE", "INVALID_CREDENTIALS", httpServletRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        user.setLastLoginAt(Instant.now());
        CurrentUser currentUser = securityUserService.toCurrentUser(user);
        securityAuditService.recordLoginEvent(user.getId(), user.getUsername(), "LOGIN", "SUCCESS", null, httpServletRequest);
        return issueTokens(user, currentUser, httpServletRequest);
    }

    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest request, HttpServletRequest httpServletRequest) {
        String tokenHash = sha256(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash).orElse(null);
        if (refreshToken == null) {
            securityAuditService.recordLoginEvent(null, null, "REFRESH", "FAILURE", "INVALID_REFRESH_TOKEN", httpServletRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid");
        }
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshToken.setRevokedAt(Instant.now());
            securityAuditService.recordLoginEvent(refreshToken.getUser().getId(), refreshToken.getUser().getUsername(), "REFRESH", "FAILURE", "EXPIRED_REFRESH_TOKEN", httpServletRequest);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        refreshToken.setRevokedAt(Instant.now());
        CurrentUser currentUser = securityUserService.toCurrentUser(refreshToken.getUser());
        securityAuditService.recordLoginEvent(refreshToken.getUser().getId(), refreshToken.getUser().getUsername(), "REFRESH", "SUCCESS", null, httpServletRequest);
        return issueTokens(refreshToken.getUser(), currentUser, httpServletRequest);
    }

    public void logout(AuthDtos.LogoutRequest request, HttpServletRequest httpServletRequest) {
        String tokenHash = sha256(request.refreshToken());
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash).orElse(null);
        if (refreshToken == null) {
            securityAuditService.recordLoginEvent(null, null, "LOGOUT", "FAILURE", "INVALID_REFRESH_TOKEN", httpServletRequest);
            return;
        }
        refreshToken.setRevokedAt(Instant.now());
        securityAuditService.recordLoginEvent(refreshToken.getUser().getId(), refreshToken.getUser().getUsername(), "LOGOUT", "SUCCESS", null, httpServletRequest);
    }

    public AuthDtos.CurrentUserResponse me(CurrentUser currentUser) {
        UserAccount user = userAccountRepository.findByIdAndIsDeletedFalse(currentUser.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        String fullName = String.join(" ",
                user.getLastName(),
                user.getFirstName(),
                user.getMiddleName() == null ? "" : user.getMiddleName()).trim();
        return new AuthDtos.CurrentUserResponse(
                currentUser.getUserId(),
                currentUser.getEmployeeId(),
                currentUser.getUsername(),
                fullName,
                currentUser.getRoles(),
                currentUser.getPermissions()
        );
    }

    private AuthDtos.TokenResponse issueTokens(UserAccount user, CurrentUser currentUser, HttpServletRequest httpServletRequest) {
        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(sha256(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(30L * 24 * 60 * 60));
        refreshToken.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        refreshToken.setIpAddress(httpServletRequest.getRemoteAddr());
        refreshTokenRepository.save(refreshToken);
        return new AuthDtos.TokenResponse(
                jwtService.generateAccessToken(currentUser),
                rawRefreshToken,
                jwtService.accessTokenExpiresInSeconds(),
                me(currentUser)
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
