package com.daryaftmanazam.daryaftcore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Annotation for method-level rate limiting.
 * Can be applied to controller methods to enforce specific rate limits.
 * 
 * Example:
 * <pre>
 * {@code @RateLimit(capacity = 5, refillTokens = 5, refillDuration = 1, refillUnit = TimeUnit.MINUTES)}
 * public ResponseEntity<?> login(...) {
 *     // ...
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Maximum number of tokens in the bucket
     */
    int capacity() default 10;
    
    /**
     * Number of tokens to refill
     */
    int refillTokens() default 10;
    
    /**
     * Duration for refill interval
     */
    long refillDuration() default 1;
    
    /**
     * Time unit for refill duration
     */
    TimeUnit refillUnit() default TimeUnit.MINUTES;
}
