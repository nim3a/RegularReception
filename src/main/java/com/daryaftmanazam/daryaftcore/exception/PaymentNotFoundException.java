package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a payment is not found.
 */
public class PaymentNotFoundException extends RuntimeException {
    
    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id);
    }
    
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
