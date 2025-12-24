package com.daryaftmanazam.daryaftcore.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global rate limiting filter for all API requests.
 * Implements IP-based rate limiting using Bucket4j token bucket algorithm.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Creates a new bucket with default rate limit configuration.
     * Default: 100 requests per minute.
     */
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Resolves bucket for the given key (IP address).
     */
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createBucket());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String key = getClientIP(request);
        Bucket bucket = resolveBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", 
                    String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(429);
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", 
                    String.valueOf(waitForRefill));
            response.getWriter().write(
                    "{\"error\":\"محدودیت تعداد درخواست\",\"retryAfter\":" + waitForRefill + "}"
            );
            log.warn("Rate limit exceeded for IP: {}", key);
        }
    }

    /**
     * Extracts client IP address from request.
     * Considers X-Forwarded-For header for proxy scenarios.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
