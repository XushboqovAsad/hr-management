package uz.hrms.auth.web.dtos;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        CurrentUserResponse user
) {
}
