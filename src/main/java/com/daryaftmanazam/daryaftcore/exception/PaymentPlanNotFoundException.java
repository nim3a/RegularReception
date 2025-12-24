package com.daryaftmanazam.daryaftcore.exception;

/**
 * Exception thrown when a payment plan is not found.
 */
public class PaymentPlanNotFoundException extends RuntimeException {
    
    public PaymentPlanNotFoundException(Long id) {
        super("Payment plan not found with id: " + id);
    }
    
    public PaymentPlanNotFoundException(String message) {
        super(message);
    }
}
