package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.ApiResponse;
import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.SmsConfig;
import com.daryaftmanazam.daryaftcore.repository.BusinessRepository;
import com.daryaftmanazam.daryaftcore.service.sms.AccountBalance;
import com.daryaftmanazam.daryaftcore.service.sms.SmsResult;
import com.daryaftmanazam.daryaftcore.service.sms.SmsService;
import com.daryaftmanazam.daryaftcore.service.sms.SmsStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for SMS operations.
 * This controller is only available when SMS service is enabled.
 */
@RestController
@RequestMapping("/api/sms")
@Tag(name = "SMS", description = "SMS operations API")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final SmsService smsService;
    private final BusinessRepository businessRepository;

    @Autowired(required = false)
    public SmsController(SmsService smsService, BusinessRepository businessRepository) {
        this.smsService = smsService;
        this.businessRepository = businessRepository;
    }

    /**
     * Check if SMS service is available.
     */
    @GetMapping("/status")
    @Operation(summary = "Check SMS service status", description = "Check if SMS service is enabled and available, includes balance info")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", smsService != null && smsService.isAvailable());
        
        if (smsService != null) {
            Optional<AccountBalance> balance = smsService.getBalance();
            balance.ifPresent(b -> {
                status.put("balance", b.getBalance());
                status.put("currency", b.getCurrency());
            });
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * Send a test SMS message.
     */
    @PostMapping("/test")
    @Operation(summary = "Send test SMS", description = "Send a test SMS message to verify configuration")
    public ResponseEntity<?> sendTestSms(@RequestBody TestSmsRequest request) {
        if (!isValidIranianPhoneNumber(request.getPhoneNumber())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "شماره تلفن نامعتبر است"));
        }

        String testMessage = "این یک پیام تستی از سیستم دریافت منظم است. زمان ارسال: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

        SmsResult result = smsService.sendSms(request.getPhoneNumber(), testMessage);

        return ResponseEntity.ok(Map.of(
                "success", result.isSuccess(),
                "messageId", result.getMessageId() != null ? result.getMessageId() : "",
                "message", result.isSuccess() ? "پیام تستی با موفقیت ارسال شد" : result.getErrorMessage()
        ));
    }

    /**
     * Update SMS configuration for a business.
     */
    @PutMapping("/config")
    @Operation(summary = "Update SMS config", description = "Update SMS configuration for a business")
    public ResponseEntity<?> updateSmsConfig(
            @RequestParam Long businessId,
            @RequestBody SmsConfigRequest request) {
        
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("کسب‌وکار یافت نشد"));

        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setProvider(request.getProvider());
        smsConfig.setApiKey(request.getApiKey());
        smsConfig.setLineNumber(request.getLineNumber());
        smsConfig.setEnabled(request.isEnabled());

        business.setSmsConfig(smsConfig);
        businessRepository.save(business);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "تنظیمات پیامک با موفقیت ذخیره شد"
        ));
    }

    /**
     * Validate Iranian phone number format.
     */
    private boolean isValidIranianPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        phone = phone.replaceAll("[\\s-]", "");
        return phone.matches("^(\\+98|0)?9\\d{9}$");
    }

    /**
     * Check if SMS service is available (deprecated - use /status instead).
     */
    @GetMapping("/available")
    @Operation(summary = "Check SMS service status", description = "Check if SMS service is enabled and available")
    @Deprecated
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailabilityStatus() {
        boolean available = smsService != null && smsService.isAvailable();
        
        Map<String, Object> status = Map.of(
                "available", available,
                "provider", available ? "MeliPayamak" : "None",
                "message", available ? "SMS service is active" : "SMS service is disabled"
        );
        
        return ResponseEntity.ok(ApiResponse.success("SMS service status retrieved", status));
    }

    /**
     * Send a single SMS.
     */
    @PostMapping("/send")
    @Operation(summary = "Send SMS", description = "Send a single SMS message to a phone number")
    public ResponseEntity<ApiResponse<SmsResult>> sendSms(@RequestBody SendSmsRequest request) {
        if (smsService == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SMS service is not available"));
        }

        log.info("Sending SMS to {} via API", request.getPhoneNumber());
        SmsResult result = smsService.sendSms(request.getPhoneNumber(), request.getMessage());

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("SMS sent successfully", result));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send SMS: " + result.getErrorMessage()));
        }
    }

    /**
     * Send bulk SMS to multiple recipients.
     */
    @PostMapping("/send/bulk")
    @Operation(summary = "Send bulk SMS", description = "Send SMS to multiple phone numbers")
    public ResponseEntity<ApiResponse<List<SmsResult>>> sendBulkSms(@RequestBody BulkSmsRequest request) {
        if (smsService == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SMS service is not available"));
        }

        log.info("Sending bulk SMS to {} recipients via API", request.getPhoneNumbers().size());
        List<SmsResult> results = smsService.sendBulkSms(
                request.getPhoneNumbers(),
                request.getMessage()
        );

        long successCount = results.stream().filter(SmsResult::isSuccess).count();
        String message = String.format("Bulk SMS completed: %d/%d successful",
                successCount, results.size());

        return ResponseEntity.ok(ApiResponse.success(message, results));
    }

    /**
     * Send template-based SMS.
     */
    @PostMapping("/send/template")
    @Operation(summary = "Send template SMS", description = "Send SMS using a predefined template")
    public ResponseEntity<ApiResponse<SmsResult>> sendTemplateSms(@RequestBody TemplateSmsRequest request) {
        if (smsService == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SMS service is not available"));
        }

        log.info("Sending template SMS '{}' to {} via API", request.getTemplateId(), request.getPhoneNumber());
        SmsResult result = smsService.sendTemplateSms(
                request.getPhoneNumber(),
                request.getTemplateId(),
                request.getParameters()
        );

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("Template SMS sent successfully", result));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send template SMS: " + result.getErrorMessage()));
        }
    }

    /**
     * Check SMS delivery status.
     */
    @GetMapping("/status/{messageId}")
    @Operation(summary = "Check SMS status", description = "Check the delivery status of a sent SMS")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus(
            @Parameter(description = "Message ID returned from send SMS") @PathVariable String messageId) {
        if (smsService == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SMS service is not available"));
        }

        SmsStatus status = smsService.checkStatus(messageId);
        
        Map<String, Object> response = Map.of(
                "messageId", messageId,
                "status", status.name(),
                "description", getStatusDescription(status)
        );

        return ResponseEntity.ok(ApiResponse.success("SMS status retrieved", response));
    }

    /**
     * Get account balance.
     */
    @GetMapping("/balance")
    @Operation(summary = "Get account balance", description = "Get SMS provider account balance")
    public ResponseEntity<ApiResponse<AccountBalance>> getBalance() {
        if (smsService == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("SMS service is not available"));
        }

        AccountBalance balance = smsService.getBalance();
        return ResponseEntity.ok(ApiResponse.success("Account balance retrieved", balance));
    }

    /**
     * Get available templates.
     */
    @GetMapping("/templates")
    @Operation(summary = "List SMS templates", description = "Get list of available SMS templates")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getTemplates() {
        List<Map<String, String>> templates = List.of(
                Map.of("id", "REMINDER", "description", "Subscription reminder", "parameters", "name, date"),
                Map.of("id", "PAYMENT_SUCCESS", "description", "Payment confirmation", "parameters", "amount"),
                Map.of("id", "PAYMENT_FAILED", "description", "Payment failure", "parameters", "none"),
                Map.of("id", "SUBSCRIPTION_CREATED", "description", "New subscription", "parameters", "name"),
                Map.of("id", "SUBSCRIPTION_EXPIRED", "description", "Expiration notice", "parameters", "name"),
                Map.of("id", "SUBSCRIPTION_RENEWED", "description", "Renewal confirmation", "parameters", "name, date")
        );

        return ResponseEntity.ok(ApiResponse.success("Templates retrieved", templates));
    }

    private String getStatusDescription(SmsStatus status) {
        return switch (status) {
            case DELIVERED -> "SMS has been delivered to the recipient";
            case SENT -> "SMS has been sent to the carrier";
            case PENDING -> "SMS is pending delivery";
            case FAILED -> "SMS delivery failed";
            case UNKNOWN -> "SMS status is unknown";
        };
    }
}

/**
 * Request DTO for sending a test SMS.
 */
@Data
class TestSmsRequest {
    private String phoneNumber;
}

/**
 * Request DTO for updating SMS configuration.
 */
@Data
class SmsConfigRequest {
    private String provider;
    private String apiKey;
    private String lineNumber;
    private boolean enabled;
}

/**
 * Request DTO for sending a single SMS.
 */
@Data
class SendSmsRequest {
    private String phoneNumber;
    private String message;
}

/**
 * Request DTO for sending bulk SMS.
 */
@Data
class BulkSmsRequest {
    private List<String> phoneNumbers;
    private String message;
}

/**
 * Request DTO for sending template SMS.
 */
@Data
class TemplateSmsRequest {
    private String phoneNumber;
    private String templateId;
    private Map<String, String> parameters;
}
