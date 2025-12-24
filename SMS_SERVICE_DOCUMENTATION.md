# SMS Service Architecture Documentation

## Overview

This document describes the SMS service architecture implemented for the DaryaftCore application. The architecture provides a flexible, provider-agnostic SMS integration layer with MeliPayamak as the initial provider.

## Architecture Components

### 1. Core Interfaces and Classes

#### SmsProvider Interface
The main abstraction layer that defines the contract for all SMS providers.

**Location**: `src/main/java/com/daryaftmanazam/daryaftcore/service/sms/SmsProvider.java`

**Methods**:
- `sendSms(String phoneNumber, String message)` - Send single SMS
- `sendBulkSms(List<String> phoneNumbers, String message)` - Send bulk SMS
- `sendTemplateSms(String phoneNumber, String templateId, Map<String, String> parameters)` - Send templated SMS
- `checkStatus(String messageId)` - Check SMS delivery status
- `getBalance()` - Get account balance

#### Supporting Classes

**SmsResult** (`SmsResult.java`)
- Represents the result of an SMS operation
- Fields: `success`, `messageId`, `errorMessage`, `sentAt`

**SmsStatus** (`SmsStatus.java`)
- Enum representing SMS delivery status
- Values: `PENDING`, `SENT`, `DELIVERED`, `FAILED`, `UNKNOWN`

**AccountBalance** (`AccountBalance.java`)
- Represents SMS provider account balance
- Fields: `balance`, `currency`

### 2. MeliPayamak Provider Implementation

**Location**: `src/main/java/com/daryaftmanazam/daryaftcore/service/sms/MeliPayamakProvider.java`

**Features**:
- ✅ Conditional activation via `@ConditionalOnProperty`
- ✅ Full REST API integration with MeliPayamak
- ✅ Comprehensive error handling
- ✅ Detailed logging for all operations
- ✅ Template-based messaging support
- ✅ Status tracking and balance checking

**API Endpoints Used**:
- `SendSMS` - Send SMS messages
- `GetDelivery` - Check delivery status
- `GetCredit` - Check account balance

**Built-in Templates**:
- `REMINDER` - Subscription reminder
- `PAYMENT_SUCCESS` - Payment confirmation
- `PAYMENT_FAILED` - Payment failure notification
- `SUBSCRIPTION_CREATED` - New subscription notification
- `SUBSCRIPTION_EXPIRED` - Expiration notification
- `SUBSCRIPTION_RENEWED` - Renewal confirmation

### 3. SMS Configuration

#### Application-Level Configuration (`application.yml`)

```yaml
sms:
  melipayamak:
    enabled: false  # Set to true to enable SMS
    api-key: c2d0e69c-2d62-488c-82ee-16180dd56c1b
    username: your-melipayamak-username
    password: your-melipayamak-password
    line-number: your-sms-line-number
```

#### Business-Level Configuration

**SmsConfig Entity** (`SmsConfig.java`)
- Embeddable entity for business-specific SMS configuration
- Allows each business to have their own SMS provider settings
- Fields: `provider`, `apiKey`, `username`, `password`, `lineNumber`, `enabled`

**Business Entity Update**
- Added `@Embedded SmsConfig smsConfig` field
- Allows per-business SMS configuration

### 4. SMS Service Facade

**Location**: `src/main/java/com/daryaftmanazam/daryaftcore/service/sms/SmsService.java`

**Purpose**: Provides a unified, easy-to-use interface for SMS operations

**Features**:
- Automatically uses the configured SMS provider
- Only available when SMS is enabled
- Simplified API for common operations
- Built-in availability checking

## Configuration Guide

### Step 1: Get MeliPayamak Credentials

1. Register at [https://panel.melipayamak.com](https://panel.melipayamak.com)
2. Obtain your:
   - Username
   - Password
   - SMS Line Number
   - API Key (provided: `c2d0e69c-2d62-488c-82ee-16180dd56c1b`)

### Step 2: Configure Application

Edit `src/main/resources/application.yml`:

```yaml
sms:
  melipayamak:
    enabled: true  # Enable SMS service
    api-key: c2d0e69c-2d62-488c-82ee-16180dd56c1b
    username: your_actual_username
    password: your_actual_password
    line-number: 5000xxxxx  # Your SMS line number
```

### Step 3: Verify Configuration

The MeliPayamak provider will only be loaded when `sms.melipayamak.enabled=true`.

## Usage Examples

### Example 1: Send Simple SMS

```java
@Service
public class NotificationService {
    
    @Autowired(required = false)
    private SmsService smsService;
    
    public void sendWelcomeSms(String phoneNumber, String customerName) {
        if (smsService != null && smsService.isAvailable()) {
            String message = "سلام " + customerName + "، به سیستم دریافت خوش آمدید!";
            SmsResult result = smsService.sendSms(phoneNumber, message);
            
            if (result.isSuccess()) {
                log.info("SMS sent successfully. Message ID: {}", result.getMessageId());
            } else {
                log.error("Failed to send SMS: {}", result.getErrorMessage());
            }
        }
    }
}
```

### Example 2: Send Template SMS

```java
public void sendPaymentConfirmation(String phoneNumber, String amount) {
    if (smsService != null) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("amount", amount);
        
        SmsResult result = smsService.sendTemplateSms(
            phoneNumber,
            "PAYMENT_SUCCESS",
            parameters
        );
        
        log.info("Payment SMS sent: {}", result.isSuccess());
    }
}
```

### Example 3: Send Bulk SMS

```java
public void sendBulkReminder(List<Customer> customers) {
    if (smsService != null) {
        List<String> phoneNumbers = customers.stream()
            .map(Customer::getPhoneNumber)
            .collect(Collectors.toList());
        
        String message = "یادآوری: اشتراک شما به زودی منقضی می‌شود.";
        
        List<SmsResult> results = smsService.sendBulkSms(phoneNumbers, message);
        
        long successCount = results.stream()
            .filter(SmsResult::isSuccess)
            .count();
        
        log.info("Bulk SMS completed: {}/{} successful", 
                 successCount, results.size());
    }
}
```

### Example 4: Check SMS Status

```java
public void checkSmsDelivery(String messageId) {
    if (smsService != null) {
        SmsStatus status = smsService.checkStatus(messageId);
        
        switch (status) {
            case DELIVERED -> log.info("SMS delivered successfully");
            case FAILED -> log.error("SMS delivery failed");
            case PENDING -> log.info("SMS is pending");
            case SENT -> log.info("SMS has been sent");
            default -> log.warn("SMS status unknown");
        }
    }
}
```

### Example 5: Check Account Balance

```java
public void checkBalance() {
    if (smsService != null) {
        AccountBalance balance = smsService.getBalance();
        log.info("Account balance: {} {}", balance.getBalance(), balance.getCurrency());
        
        if (balance.getBalance() < 10000) {
            log.warn("Low SMS balance! Please recharge.");
        }
    }
}
```

## Database Schema Updates

The Business entity now includes SMS configuration fields:

```sql
-- Added columns to businesses table
ALTER TABLE businesses ADD COLUMN sms_provider VARCHAR(50);
ALTER TABLE businesses ADD COLUMN sms_api_key VARCHAR(200);
ALTER TABLE businesses ADD COLUMN sms_username VARCHAR(100);
ALTER TABLE businesses ADD COLUMN sms_password VARCHAR(100);
ALTER TABLE businesses ADD COLUMN sms_line_number VARCHAR(20);
ALTER TABLE businesses ADD COLUMN sms_enabled BOOLEAN DEFAULT FALSE;
```

These will be created automatically by Hibernate when `ddl-auto: update` is set.

## Extending with New Providers

To add a new SMS provider (e.g., Kavenegar):

### Step 1: Create Provider Implementation

```java
@Service
@ConditionalOnProperty(name = "sms.kavenegar.enabled", havingValue = "true")
@Slf4j
public class KavenegarProvider implements SmsProvider {
    
    @Value("${sms.kavenegar.api-key}")
    private String apiKey;
    
    // Implement all interface methods
    @Override
    public SmsResult sendSms(String phoneNumber, String message) {
        // Implementation
    }
    
    // ... other methods
}
```

### Step 2: Add Configuration

```yaml
sms:
  kavenegar:
    enabled: false
    api-key: your-kavenegar-api-key
```

### Step 3: Use Provider

The `SmsService` will automatically use whichever provider is enabled.

## Logging

All SMS operations are logged with the following levels:

- **INFO**: Successful operations, SMS sent confirmation
- **DEBUG**: Status checks, API requests
- **ERROR**: Failed operations, API errors
- **WARN**: Low balance warnings, unknown statuses

Example log output:
```
2025-12-23 14:30:45 - SMS Service initialized with provider: MeliPayamakProvider
2025-12-23 14:30:50 - Sending SMS to 09123456789 via MeliPayamak
2025-12-23 14:30:51 - SMS sent successfully to 09123456789. Message ID: 12345678
```

## Error Handling

The implementation handles various error scenarios:

1. **Network Errors**: Caught and returned in `SmsResult.errorMessage`
2. **API Errors**: MeliPayamak error codes mapped to error messages
3. **Invalid Configuration**: Provider not loaded if config is invalid
4. **Service Unavailable**: `SmsService` can be null-checked before use

Best practice:
```java
@Autowired(required = false)
private SmsService smsService;

public void sendSms(String phone, String message) {
    if (smsService != null && smsService.isAvailable()) {
        SmsResult result = smsService.sendSms(phone, message);
        if (!result.isSuccess()) {
            // Handle error
            log.error("SMS failed: {}", result.getErrorMessage());
        }
    } else {
        log.warn("SMS service not available");
    }
}
```

## Security Considerations

1. **Credentials Storage**: SMS credentials are stored in `application.yml`
   - For production, use environment variables or secret management
   - Example: `${SMS_USERNAME:default-value}`

2. **API Key Protection**: The API key should be kept secure
   - Don't commit real credentials to version control
   - Use Spring profiles for different environments

3. **Rate Limiting**: Consider implementing rate limiting to prevent abuse

4. **Phone Number Validation**: Validate phone numbers before sending SMS

## Performance Considerations

1. **Bulk SMS**: The current implementation sends bulk SMS sequentially
   - Consider implementing parallel processing for large batches

2. **Async Processing**: SMS sending is synchronous
   - For non-critical SMS, consider using `@Async` annotation

3. **Caching**: Template messages are stored in memory
   - For production, consider loading from database or configuration

## Testing

### Disabling SMS in Tests

```yaml
# application-test.yml
sms:
  melipayamak:
    enabled: false
```

### Mock SMS Service

```java
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public SmsProvider mockSmsProvider() {
        return mock(SmsProvider.class);
    }
}
```

## Monitoring and Maintenance

### Recommended Monitoring

1. **Balance Monitoring**: Check balance regularly
2. **Delivery Rate**: Track SMS delivery success rate
3. **Error Tracking**: Monitor failed SMS attempts
4. **Cost Tracking**: Track SMS usage per business

### Health Check Example

```java
@Component
public class SmsHealthIndicator implements HealthIndicator {
    
    @Autowired(required = false)
    private SmsService smsService;
    
    @Override
    public Health health() {
        if (smsService == null) {
            return Health.down().withDetail("status", "disabled").build();
        }
        
        AccountBalance balance = smsService.getBalance();
        if (balance.getBalance() < 10000) {
            return Health.down()
                .withDetail("balance", balance.getBalance())
                .withDetail("status", "low balance")
                .build();
        }
        
        return Health.up()
            .withDetail("balance", balance.getBalance())
            .withDetail("status", "active")
            .build();
    }
}
```

## Troubleshooting

### SMS Not Being Sent

1. Check if SMS is enabled: `sms.melipayamak.enabled=true`
2. Verify credentials are correct
3. Check application logs for errors
4. Verify account balance
5. Test API credentials directly with MeliPayamak panel

### Provider Bean Not Found

If you get "No qualifying bean of type 'SmsProvider'":
- SMS is disabled by default
- Set `sms.melipayamak.enabled=true` in `application.yml`

### Invalid Phone Number Format

MeliPayamak expects Iranian phone numbers:
- Format: `09xxxxxxxxx` (11 digits)
- Validate before sending

## Future Enhancements

1. **Database Template Storage**: Store SMS templates in database
2. **Scheduled SMS**: Queue and schedule SMS for future delivery
3. **SMS History**: Track all sent SMS messages
4. **Delivery Webhooks**: Receive real-time delivery status updates
5. **Multi-Provider Failover**: Automatic failover between providers
6. **Analytics Dashboard**: SMS usage and delivery analytics
7. **A/B Testing**: Test different message templates

## API Reference

### Available Templates

| Template ID | Description | Parameters |
|------------|-------------|------------|
| REMINDER | Subscription reminder | name, date |
| PAYMENT_SUCCESS | Payment confirmation | amount |
| PAYMENT_FAILED | Payment failure | - |
| SUBSCRIPTION_CREATED | New subscription | name |
| SUBSCRIPTION_EXPIRED | Expiration notice | name |
| SUBSCRIPTION_RENEWED | Renewal confirmation | name, date |

### MeliPayamak Status Codes

| Code | Status | Meaning |
|------|--------|---------|
| 1 | DELIVERED | SMS delivered to recipient |
| 2 | FAILED | SMS delivery failed |
| 8 | SENT | SMS sent to carrier |
| 16 | PENDING | SMS is pending |

## Support

For MeliPayamak support:
- Website: https://panel.melipayamak.com
- Documentation: https://docs.melipayamak.com

For application issues:
- Check logs: `logging.level.com.daryaftmanazam.daryaftcore.service.sms: DEBUG`
- Review this documentation
- Contact development team

---

**Last Updated**: December 23, 2025
**Version**: 1.0.0
