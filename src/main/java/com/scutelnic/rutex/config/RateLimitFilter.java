package com.scutelnic.rutex.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 60;
    private static final long WINDOW_MS = 60_000L;
    private static final long STALE_ENTRY_TTL_MS = 10 * WINDOW_MS;
    private static final long CLEANUP_INTERVAL_MS = 60_000L;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private volatile long lastCleanupMs = 0L;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long now = System.currentTimeMillis();
        maybeCleanup(now);

        String clientIp = getClientIpAddress(request);
        WindowCounter counter = counters.compute(clientIp, (key, current) -> {
            if (current == null || (now - current.windowStartMs) >= WINDOW_MS) {
                WindowCounter fresh = new WindowCounter();
                fresh.windowStartMs = now;
                fresh.count = 1;
                fresh.lastSeenMs = now;
                return fresh;
            }
            current.count++;
            current.lastSeenMs = now;
            return current;
        });

        if (counter.count > MAX_REQUESTS_PER_WINDOW) {
            long retryAfterSeconds = Math.max(1L, ((counter.windowStartMs + WINDOW_MS) - now + 999) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void maybeCleanup(long now) {
        if ((now - lastCleanupMs) < CLEANUP_INTERVAL_MS) {
            return;
        }
        lastCleanupMs = now;
        counters.entrySet().removeIf(entry -> (now - entry.getValue().lastSeenMs) > STALE_ENTRY_TTL_MS);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private static class WindowCounter {
        private long windowStartMs;
        private int count;
        private long lastSeenMs;
    }
}
