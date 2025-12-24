package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a payment operation is invalid.
 */
public class InvalidPaymentException extends RuntimeException {
    
    public InvalidPaymentException(String message) {
        super(message);
    }
}
