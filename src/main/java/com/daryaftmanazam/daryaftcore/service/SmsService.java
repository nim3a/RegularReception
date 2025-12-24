package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.config.SmsProperties;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Payment;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.service.sms.SmsProvider;
import com.daryaftmanazam.daryaftcore.dto.response.AccountBalance;
import com.daryaftmanazam.daryaftcore.dto.response.SmsResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SMS Service Facade
 * 
 * Provides a unified interface for sending SMS messages with business logic.
 * Handles SMS provider integration, validation, templates, and graceful degradation
 * when SMS is disabled.
 * 
 * Features:
 * - Automatic enable/disable handling
 * - Phone number validation
 * - Template-based messaging
 * - Bulk SMS support
 * - Business-specific helpers (reminders, payment notifications)
 * - Comprehensive logging
 * - Never throws exceptions to callers
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    @Autowired(required = false)
    private final Optional<SmsProvider> smsProvider;

    private final SmsProperties smsProperties;

    @Value("${sms.melipayamak.enabled:false}")
    private boolean enabled;

    /**
     * Send SMS with automatic enable/disable handling
     * 
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @return SmsResult with success status and message ID or error
     */
    public SmsResult send(String phoneNumber, String message) {
        if (!enabled || smsProvider.isEmpty()) {
            log.info("📱 SMS DISABLED - Would send to {}: {}", phoneNumber, message);
            return new SmsResult(
                true,
                "SIMULATED-" + System.currentTimeMillis(),
                null,
                LocalDateTime.now()
            );
        }

        // Validate phone number
        if (!isValidIranianPhoneNumber(phoneNumber)) {
            log.warn("Invalid phone number: {}", phoneNumber);
            return new SmsResult(
                false,
                null,
                "شماره تلفن نامعتبر است",
                LocalDateTime.now()
            );
        }

        try {
            SmsResult result = smsProvider.get().sendSms(phoneNumber, message);
            if (result.isSuccess()) {
                log.info("✅ SMS sent successfully to {} - MessageId: {}",
                    phoneNumber, result.getMessageId());
            } else {
                log.error("❌ SMS failed to {} - Error: {}",
                    phoneNumber, result.getErrorMessage());
            }
            return result;
        } catch (Exception e) {
            log.error("Exception while sending SMS to {}", phoneNumber, e);
            return new SmsResult(
                false,
                null,
                e.getMessage(),
                LocalDateTime.now()
            );
        }
    }

    /**
     * Send templated SMS
     * 
     * @param phoneNumber Recipient phone number
     * @param templateKey Template identifier from configuration
     * @param parameters Template parameters to replace
     * @return SmsResult with success status
     */
    public SmsResult sendTemplate(String phoneNumber, String templateKey,
                                  Map<String, String> parameters) {
        String template = smsProperties.getTemplate(templateKey);
        if (template == null || template.isEmpty()) {
            log.error("Template not found: {}", templateKey);
            return new SmsResult(
                false,
                null,
                "قالب پیام یافت نشد",
                LocalDateTime.now()
            );
        }

        // Replace parameters
        String message = template;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return send(phoneNumber, message);
    }

    /**
     * Send bulk SMS to multiple recipients
     * 
     * @param phoneNumbers List of recipient phone numbers
     * @param message Message content
     * @return List of SmsResult for each recipient
     */
    public List<SmsResult> sendBulk(List<String> phoneNumbers, String message) {
        if (!enabled || smsProvider.isEmpty()) {
            log.info("📱 SMS DISABLED - Would send bulk to {} numbers", phoneNumbers.size());
            return phoneNumbers.stream()
                .map(phone -> new SmsResult(
                    true,
                    "SIMULATED-" + System.currentTimeMillis(),
                    null,
                    LocalDateTime.now()
                ))
                .toList();
        }

        return smsProvider.get().sendBulkSms(phoneNumbers, message);
    }

    /**
     * Send reminder SMS for subscription expiration
     * 
     * @param customer Customer to notify
     * @param subscription Expiring subscription
     * @return SmsResult with success status
     */
    public SmsResult sendReminderSms(Customer customer, Subscription subscription) {
        Map<String, String> params = Map.of(
            "name", customer.getFullName(),
            "date", subscription.getEndDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        );
        return sendTemplate(customer.getPhone(), "reminder", params);
    }

    /**
     * Send payment success notification SMS
     * 
     * @param customer Customer who made payment
     * @param payment Payment details
     * @return SmsResult with success status
     */
    public SmsResult sendPaymentSuccessSms(Customer customer, Payment payment) {
        Map<String, String> params = Map.of(
            "amount", String.format("%,d", payment.getAmount().longValue()),
            "transactionId", payment.getTransactionId()
        );
        return sendTemplate(customer.getPhone(), "payment-success", params);
    }

    /**
     * Get account balance (if SMS enabled)
     * 
     * @return Optional containing balance information if available
     */
    public Optional<AccountBalance> getBalance() {
        if (enabled && smsProvider.isPresent()) {
            try {
                return Optional.of(smsProvider.get().getBalance());
            } catch (Exception e) {
                log.error("Error getting SMS balance", e);
            }
        }
        return Optional.empty();
    }

    /**
     * Validate Iranian phone number format
     * 
     * Accepts formats:
     * - 09xxxxxxxxx
     * - 9xxxxxxxxx
     * - +989xxxxxxxxx
     * - 0989xxxxxxxxx
     * 
     * @param phone Phone number to validate
     * @return true if valid Iranian mobile number
     */
    private boolean isValidIranianPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        // Remove spaces and dashes
        phone = phone.replaceAll("[\\s-]", "");

        // Check format: 09xxxxxxxxx or 9xxxxxxxxx
        return phone.matches("^(\\+98|0)?9\\d{9}$");
    }
}
