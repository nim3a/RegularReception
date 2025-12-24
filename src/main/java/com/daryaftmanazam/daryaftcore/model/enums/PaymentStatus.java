package com.daryaftmanazam.daryaftcore.model.enums;

/**
 * Represents the status of a payment.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    COMPLETED,  // Kept for backward compatibility
    REFUNDED
}
