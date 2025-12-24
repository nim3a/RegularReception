package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * SMS Service facade that provides a unified interface for sending SMS.
 * This service automatically uses the configured SMS provider.
 * 
 * Usage:
 * <pre>
 * {@code
 * @Autowired
 * private SmsService smsService;
 * 
 * // Send simple SMS
 * SmsResult result = smsService.sendSms("09123456789", "Hello!");
 * 
 * // Send template SMS
 * Map<String, String> params = Map.of("name", "احمد", "date", "1403/10/15");
 * SmsResult result = smsService.sendTemplateSms("09123456789", "REMINDER", params);
 * }
 * </pre>
 */
@Service("smsProviderService")
@ConditionalOnBean(SmsProvider.class)
@Slf4j
public class SmsService {

    private final SmsProvider smsProvider;

    @Autowired
    public SmsService(SmsProvider smsProvider) {
        this.smsProvider = smsProvider;
        log.info("SMS Service initialized with provider: {}", smsProvider.getClass().getSimpleName());
    }

    /**
     * Send a single SMS message.
     *
     * @param phoneNumber The recipient's phone number
     * @param message     The message to send
     * @return SmsResult containing the result of the operation
     */
    public SmsResult sendSms(String phoneNumber, String message) {
        log.info("Sending SMS to {}", phoneNumber);
        return smsProvider.sendSms(phoneNumber, message);
    }

    /**
     * Send bulk SMS to multiple recipients.
     *
     * @param phoneNumbers List of recipient phone numbers
     * @param message      The message to send
     * @return List of SmsResult for each recipient
     */
    public List<SmsResult> sendBulkSms(List<String> phoneNumbers, String message) {
        log.info("Sending bulk SMS to {} recipients", phoneNumbers.size());
        return smsProvider.sendBulkSms(phoneNumbers, message);
    }

    /**
     * Send templated SMS using predefined templates.
     *
     * @param phoneNumber The recipient's phone number
     * @param templateId  The template identifier
     * @param parameters  Template parameters for variable replacement
     * @return SmsResult containing the result of the operation
     */
    public SmsResult sendTemplateSms(String phoneNumber, String templateId, Map<String, String> parameters) {
        log.info("Sending template SMS '{}' to {}", templateId, phoneNumber);
        return smsProvider.sendTemplateSms(phoneNumber, templateId, parameters);
    }

    /**
     * Check the delivery status of a sent SMS.
     *
     * @param messageId The message ID returned from sendSms
     * @return SmsStatus indicating the current status
     */
    public SmsStatus checkStatus(String messageId) {
        return smsProvider.checkStatus(messageId);
    }

    /**
     * Get the account balance from the SMS provider.
     *
     * @return AccountBalance containing balance information
     */
    public AccountBalance getBalance() {
        return smsProvider.getBalance();
    }

    /**
     * Check if SMS service is available.
     *
     * @return true if SMS provider is configured and available
     */
    public boolean isAvailable() {
        return smsProvider != null;
    }
}
