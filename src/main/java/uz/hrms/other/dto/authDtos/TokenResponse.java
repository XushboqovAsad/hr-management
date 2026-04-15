package uz.hrms.other.dto.authDtos;

public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds, CurrentUserResponse user) {
}
