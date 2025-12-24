package com.daryaftmanazam.daryaftcore.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MeliPayamak SMS provider implementation.
 * This service is only active when sms.melipayamak.enabled=true in configuration.
 */
@Service
@ConditionalOnProperty(name = "sms.melipayamak.enabled", havingValue = "true")
@Slf4j
public class MeliPayamakProvider implements SmsProvider {

    private static final String BASE_URL = "https://rest.payamak-panel.com/api/SendSMS/";

    @Value("${sms.melipayamak.api-key}")
    private String apiKey;

    @Value("${sms.melipayamak.username}")
    private String username;

    @Value("${sms.melipayamak.password}")
    private String password;

    @Value("${sms.melipayamak.line-number}")
    private String lineNumber;

    private final RestTemplate restTemplate;

    public MeliPayamakProvider() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String message) {
        try {
            log.info("Sending SMS to {} via MeliPayamak", phoneNumber);

            // Prepare request
            Map<String, Object> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);
            request.put("from", lineNumber);
            request.put("to", phoneNumber);
            request.put("text", message);
            request.put("isFlash", false);

            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<MeliPayamakResponse> response = restTemplate.postForEntity(
                    BASE_URL + "SendSMS",
                    entity,
                    MeliPayamakResponse.class
            );

            MeliPayamakResponse body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK &&
                    body != null &&
                    body.getRetStatus() == 1) {

                log.info("SMS sent successfully to {}. Message ID: {}", phoneNumber, body.getValue());
                return new SmsResult(
                        true,
                        String.valueOf(body.getValue()),
                        null,
                        LocalDateTime.now()
                );
            } else {
                String errorMsg = body != null ? body.getStrRetStatus() : "Unknown error";
                log.error("Failed to send SMS to {}: {}", phoneNumber, errorMsg);
                return new SmsResult(
                        false,
                        null,
                        errorMsg,
                        LocalDateTime.now()
                );
            }
        } catch (Exception e) {
            log.error("Error sending SMS via MeliPayamak to {}", phoneNumber, e);
            return new SmsResult(false, null, e.getMessage(), LocalDateTime.now());
        }
    }

    @Override
    public List<SmsResult> sendBulkSms(List<String> phoneNumbers, String message) {
        log.info("Sending bulk SMS to {} recipients via MeliPayamak", phoneNumbers.size());
        List<SmsResult> results = new ArrayList<>();

        for (String phoneNumber : phoneNumbers) {
            results.add(sendSms(phoneNumber, message));
        }

        long successCount = results.stream().filter(SmsResult::isSuccess).count();
        log.info("Bulk SMS completed. Success: {}/{}", successCount, phoneNumbers.size());

        return results;
    }

    @Override
    public SmsResult sendTemplateSms(String phoneNumber, String templateId, Map<String, String> parameters) {
        log.info("Sending template SMS '{}' to {} via MeliPayamak", templateId, phoneNumber);

        // Build message from template
        String message = buildMessageFromTemplate(templateId, parameters);

        if (message == null || message.isEmpty()) {
            log.error("Template not found: {}", templateId);
            return new SmsResult(false, null, "Template not found: " + templateId, LocalDateTime.now());
        }

        return sendSms(phoneNumber, message);
    }

    @Override
    public SmsStatus checkStatus(String messageId) {
        try {
            log.debug("Checking SMS status for message ID: {}", messageId);

            Map<String, Object> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);
            request.put("messageId", Long.parseLong(messageId));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<MeliPayamakStatusResponse> response = restTemplate.postForEntity(
                    BASE_URL + "GetDelivery",
                    entity,
                    MeliPayamakStatusResponse.class
            );

            MeliPayamakStatusResponse statusBody = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && statusBody != null) {
                SmsStatus status = mapMeliPayamakStatus(statusBody.getStatus());
                log.debug("SMS status for message ID {}: {}", messageId, status);
                return status;
            }

            log.warn("Could not determine SMS status for message ID: {}", messageId);
            return SmsStatus.UNKNOWN;
        } catch (Exception e) {
            log.error("Error checking SMS status for message ID: {}", messageId, e);
            return SmsStatus.UNKNOWN;
        }
    }

    @Override
    public AccountBalance getBalance() {
        try {
            log.debug("Retrieving account balance from MeliPayamak");

            Map<String, Object> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<MeliPayamakBalanceResponse> response = restTemplate.postForEntity(
                    BASE_URL + "GetCredit",
                    entity,
                    MeliPayamakBalanceResponse.class
            );

            MeliPayamakBalanceResponse balanceBody = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && balanceBody != null) {
                double balance = balanceBody.getValue();
                log.info("Account balance retrieved: {} IRR", balance);
                return new AccountBalance(balance, "IRR");
            }

            log.warn("Could not retrieve account balance");
            return new AccountBalance(0, "IRR");
        } catch (Exception e) {
            log.error("Error getting account balance from MeliPayamak", e);
            return new AccountBalance(0, "IRR");
        }
    }

    /**
     * Build message from template by replacing parameters.
     */
    private String buildMessageFromTemplate(String templateId, Map<String, String> parameters) {
        String template = getTemplate(templateId);

        if (template == null || template.isEmpty()) {
            return null;
        }

        // Replace parameters in template
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return template;
    }

    /**
     * Get predefined SMS templates.
     * Templates can be enhanced to be loaded from database or configuration file.
     */
    private String getTemplate(String templateId) {
        Map<String, String> templates = Map.of(
                "REMINDER", "مشتری گرامی {name}، اشتراک شما تا {date} اعتبار دارد.",
                "PAYMENT_SUCCESS", "پرداخت شما به مبلغ {amount} تومان با موفقیت انجام شد.",
                "PAYMENT_FAILED", "پرداخت شما ناموفق بود. لطفاً مجدداً تلاش کنید.",
                "SUBSCRIPTION_CREATED", "مشتری گرامی {name}، اشتراک شما با موفقیت ایجاد شد.",
                "SUBSCRIPTION_EXPIRED", "مشتری گرامی {name}، اشتراک شما منقضی شده است.",
                "SUBSCRIPTION_RENEWED", "مشتری گرامی {name}، اشتراک شما تا {date} تمدید شد."
        );

        return templates.getOrDefault(templateId, null);
    }

    /**
     * Map MeliPayamak status codes to SmsStatus enum.
     */
    private SmsStatus mapMeliPayamakStatus(int status) {
        return switch (status) {
            case 1 -> SmsStatus.DELIVERED;
            case 2 -> SmsStatus.FAILED;
            case 8 -> SmsStatus.SENT;
            case 16 -> SmsStatus.PENDING;
            default -> SmsStatus.UNKNOWN;
        };
    }
}
