package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when payment processing fails.
 */
public class PaymentProcessingException extends RuntimeException {
    
    public PaymentProcessingException(String message) {
        super(message);
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PaymentProcessingException(Long paymentId, String reason) {
        super(String.format("Payment processing failed for payment id %d: %s", paymentId, reason));
    }
}
