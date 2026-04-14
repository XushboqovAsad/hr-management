package uz.hrms.auth.web;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record LogoutRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record CurrentUserResponse(
            UUID userId,
            UUID employeeId,
            String username,
            String fullName,
            Set<String> roles,
            Set<String> permissions
    ) {
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            CurrentUserResponse user
    ) {
    }
}
