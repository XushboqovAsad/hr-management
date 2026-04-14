package uz.hrms.auth.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.hrms.auth.service.AuthService;
import uz.hrms.security.CurrentUser;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT tokens")
    public ResponseEntity<AuthDtos.TokenResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request,
                                                        HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthDtos.TokenResponse> refresh(@Valid @RequestBody AuthDtos.RefreshRequest request,
                                                          HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.refresh(request, httpServletRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody AuthDtos.LogoutRequest request,
                                       HttpServletRequest httpServletRequest) {
        authService.logout(request, httpServletRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user context")
    public ResponseEntity<AuthDtos.CurrentUserResponse> me(@AuthenticationPrincipal CurrentUser currentUser) {
        return ResponseEntity.ok(authService.me(currentUser));
    }
}
