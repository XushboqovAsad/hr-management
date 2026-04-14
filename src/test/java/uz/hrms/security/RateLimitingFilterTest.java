package uz.hrms.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    @Test
    void shouldReturnTooManyRequestsAfterLimitExceeded() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
        MockHttpServletResponse lastResponse = null;

        for (int index = 0; index < 11; index++) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr("127.0.0.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            lastResponse = response;
        }

        assertThat(lastResponse).isNotNull();
        assertThat(lastResponse.getStatus()).isEqualTo(429);
        assertThat(lastResponse.getHeader("Retry-After")).isEqualTo("60");
    }
}
