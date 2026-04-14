package uz.hrms;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, FixedWindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return resolveLimit(request) == 0;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        int limit = resolveLimit(request);
        String key = request.getRemoteAddr() + "|" + bucketKey(request);
        long currentWindow = Instant.now().getEpochSecond() / 60L;
        FixedWindowCounter counter = counters.compute(key, (ignored, existing) -> {
            boolean sameWindow = existing != null && existing.windowMinute() == currentWindow;
            if (sameWindow == false) {
                return new FixedWindowCounter(currentWindow, new AtomicInteger(1));
            }
            existing.counter().incrementAndGet();
            return existing;
        });
        int currentCount = counter.counter().get();
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));
        if (currentCount > limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Too many requests");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private int resolveLimit(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("/api/v1/auth/login".equals(uri)) {
            return 10;
        }
        if ("/api/v1/auth/refresh".equals(uri)) {
            return 30;
        }
        if (uri.contains("/documents/") && (uri.endsWith("/download") || uri.endsWith("/preview"))) {
            return 120;
        }
        return 0;
    }

    private String bucketKey(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/documents/") && (uri.endsWith("/download") || uri.endsWith("/preview"))) {
            return "DOCUMENT_ACCESS";
        }
        return uri;
    }

    private record FixedWindowCounter(long windowMinute, AtomicInteger counter) {
    }
}
