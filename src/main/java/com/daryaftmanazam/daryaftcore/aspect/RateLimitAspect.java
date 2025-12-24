package com.daryaftmanazam.daryaftcore.aspect;

import com.daryaftmanazam.daryaftcore.annotation.RateLimit;
import com.daryaftmanazam.daryaftcore.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Aspect for method-level rate limiting using @RateLimit annotation.
 * Creates separate buckets for each IP + method combination.
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Intercepts methods annotated with @RateLimit and applies rate limiting.
     */
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return pjp.proceed();
        }

        String key = getKey(request, pjp);
        Bucket bucket = resolveBucket(key, rateLimit);

        if (bucket.tryConsume(1)) {
            return pjp.proceed();
        } else {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new RateLimitExceededException("درخواست‌های بیش از حد. لطفاً کمی صبر کنید.");
        }
    }

    /**
     * Resolves or creates a bucket for the given key with specified rate limit configuration.
     */
    private Bucket resolveBucket(String key, RateLimit rateLimit) {
        return buckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(rateLimit.capacity())
                    .refillIntervally(
                            rateLimit.refillTokens(),
                            Duration.of(rateLimit.refillDuration(), 
                                    toChronoUnit(rateLimit.refillUnit()))
                    )
                    .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Generates a unique key based on IP address and method name.
     */
    private String getKey(HttpServletRequest request, ProceedingJoinPoint pjp) {
        String ip = request.getRemoteAddr();
        String method = pjp.getSignature().getName();
        return ip + ":" + method;
    }

    /**
     * Retrieves the current HTTP request from RequestContextHolder.
     */
    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        return null;
    }

    /**
     * Converts TimeUnit to ChronoUnit for Duration creation.
     */
    private ChronoUnit toChronoUnit(TimeUnit timeUnit) {
        return switch (timeUnit) {
            case SECONDS -> ChronoUnit.SECONDS;
            case MINUTES -> ChronoUnit.MINUTES;
            case HOURS -> ChronoUnit.HOURS;
            case DAYS -> ChronoUnit.DAYS;
            default -> ChronoUnit.SECONDS;
        };
    }
}
