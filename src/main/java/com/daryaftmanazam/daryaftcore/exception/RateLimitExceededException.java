package com.daryaftmanazam.daryaftcore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when rate limit is exceeded.
 * Returns HTTP 429 Too Many Requests status.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {
    
    public RateLimitExceededException(String message) {
        super(message);
    }
}
