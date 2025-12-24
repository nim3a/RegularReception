package com.daryaftmanazam.daryaftcore.service.sms;

import java.util.List;
import java.util.Map;

/**
 * Interface for SMS providers.
 * This abstraction layer allows for easy integration of multiple SMS providers.
 */
public interface SmsProvider {

    /**
     * Send single SMS to a phone number.
     *
     * @param phoneNumber The recipient's phone number
     * @param message     The message to send
     * @return SmsResult containing the result of the operation
     */
    SmsResult sendSms(String phoneNumber, String message);

    /**
     * Send bulk SMS to multiple phone numbers.
     *
     * @param phoneNumbers List of recipient phone numbers
     * @param message      The message to send
     * @return List of SmsResult for each recipient
     */
    List<SmsResult> sendBulkSms(List<String> phoneNumbers, String message);

    /**
     * Send templated SMS using predefined templates.
     *
     * @param phoneNumber The recipient's phone number
     * @param templateId  The template identifier
     * @param parameters  Template parameters for variable replacement
     * @return SmsResult containing the result of the operation
     */
    SmsResult sendTemplateSms(String phoneNumber, String templateId, Map<String, String> parameters);

    /**
     * Check SMS delivery status.
     *
     * @param messageId The message ID returned from sendSms
     * @return SmsStatus indicating the current status
     */
    SmsStatus checkStatus(String messageId);

    /**
     * Get account balance from SMS provider.
     *
     * @return AccountBalance containing balance information
     */
    AccountBalance getBalance();
}
