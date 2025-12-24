package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents the result of an SMS operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsResult {

    /**
     * Indicates whether the SMS was sent successfully.
     */
    private boolean success;

    /**
     * The message ID assigned by the SMS provider.
     */
    private String messageId;

    /**
     * Error message if the operation failed.
     */
    private String errorMessage;

    /**
     * Timestamp when the SMS was sent.
     */
    private LocalDateTime sentAt;
}
