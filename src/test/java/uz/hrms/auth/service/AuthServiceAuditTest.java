package uz.hrms.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import uz.hrms.audit.SecurityAuditService;
import uz.hrms.auth.entity.UserAccount;
import uz.hrms.auth.repository.RefreshTokenRepository;
import uz.hrms.auth.repository.UserAccountRepository;
import uz.hrms.auth.web.AuthDtos;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceAuditTest {

    @Test
    void shouldAuditFailedLoginForUnknownUser() {
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        SecurityUserService securityUserService = mock(SecurityUserService.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        SecurityAuditService securityAuditService = mock(SecurityAuditService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(userAccountRepository.findByUsernameIgnoreCaseAndIsDeletedFalse("ghost")).thenReturn(Optional.empty());

        AuthService authService = new AuthService(
                userAccountRepository,
                refreshTokenRepository,
                securityUserService,
                jwtService,
                passwordEncoder,
                securityAuditService
        );

        assertThatThrownBy(() -> authService.login(new AuthDtos.LoginRequest("ghost", "Password123"), request))
                .isInstanceOf(ResponseStatusException.class);

        verify(securityAuditService).recordLoginEvent(eq(null), eq("ghost"), eq("LOGIN"), eq("FAILURE"), eq("INVALID_CREDENTIALS"), eq(request));
    }

    @Test
    void shouldAuditSuccessfulLogin() {
        UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
        SecurityUserService securityUserService = mock(SecurityUserService.class);
        JwtService jwtService = mock(JwtService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        SecurityAuditService securityAuditService = mock(SecurityAuditService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        UUID userId = UUID.randomUUID();
        UserAccount user = mock(UserAccount.class);
        uz.hrms.security.CurrentUser currentUser = mock(uz.hrms.security.CurrentUser.class);

        when(userAccountRepository.findByUsernameIgnoreCaseAndIsDeletedFalse("employee")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(securityUserService.toCurrentUser(user)).thenReturn(currentUser);
        when(jwtService.generateAccessToken(currentUser)).thenReturn("token");
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(900L);
        when(userAccountRepository.findByIdAndIsDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("employee");
        when(user.getPasswordHash()).thenReturn("hash");
        when(user.isActive()).thenReturn(true);
        when(user.getFirstName()).thenReturn("Test");
        when(user.getLastName()).thenReturn("User");
        when(user.getMiddleName()).thenReturn(null);
        when(currentUser.getUserId()).thenReturn(userId);
        when(currentUser.getEmployeeId()).thenReturn(null);
        when(currentUser.getUsername()).thenReturn("employee");
        when(currentUser.getRoles()).thenReturn(Set.of("EMPLOYEE"));
        when(currentUser.getPermissions()).thenReturn(Set.of("AUTH:READ"));
        when(request.getHeader("User-Agent")).thenReturn("JUnit");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthService authService = new AuthService(
                userAccountRepository,
                refreshTokenRepository,
                securityUserService,
                jwtService,
                passwordEncoder,
                securityAuditService
        );

        authService.login(new AuthDtos.LoginRequest("employee", "Password123"), request);

        verify(securityAuditService).recordLoginEvent(eq(userId), eq("employee"), eq("LOGIN"), eq("SUCCESS"), eq(null), eq(request));
    }
}
