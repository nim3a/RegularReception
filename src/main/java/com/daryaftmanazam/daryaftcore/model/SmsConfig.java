package com.daryaftmanazam.daryaftcore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable SMS configuration entity.
 * Can be embedded in Business entity to store business-specific SMS settings.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsConfig {

    /**
     * SMS provider name (e.g., "melipayamak", "kavenegar").
     */
    @Column(name = "sms_provider", length = 50)
    private String provider;

    /**
     * API key for the SMS provider.
     */
    @Column(name = "sms_api_key", length = 200)
    private String apiKey;

    /**
     * Username for the SMS provider account.
     */
    @Column(name = "sms_username", length = 100)
    private String username;

    /**
     * Password for the SMS provider account.
     */
    @Column(name = "sms_password", length = 100)
    private String password;

    /**
     * SMS line number (sender number).
     */
    @Column(name = "sms_line_number", length = 20)
    private String lineNumber;

    /**
     * Whether SMS is enabled for this business.
     */
    @Column(name = "sms_enabled")
    private Boolean enabled = false;
}
