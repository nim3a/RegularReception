package com.daryaftmanazam.daryaftcore.service.sms;

/**
 * Enum representing SMS delivery status.
 */
public enum SmsStatus {
    /**
     * SMS is pending and not yet sent.
     */
    PENDING,

    /**
     * SMS has been sent to the recipient.
     */
    SENT,

    /**
     * SMS has been delivered to the recipient.
     */
    DELIVERED,

    /**
     * SMS delivery failed.
     */
    FAILED,

    /**
     * SMS status is unknown or could not be determined.
     */
    UNKNOWN
}
