package uz.hrms;

import org.junit.jupiter.api.Test;
import uz.hrms.other.CurrentUser;
import uz.hrms.other.JwtProperties;
import uz.hrms.other.JwtService;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void shouldGenerateAndParseAccessToken() {
        JwtService jwtService = new JwtService(new JwtProperties("test", Duration.ofMinutes(15), Duration.ofDays(7), "test-secret-test-secret-test-secret-test-secret-123456"));
        UUID userId = UUID.randomUUID();
        CurrentUser currentUser = new CurrentUser(userId, UUID.randomUUID(), "tester", "hash", true, Set.of("HR_ADMIN"), Set.of("EMPLOYEE:READ"));

        String token = jwtService.generateAccessToken(currentUser);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }
}
