# SMS Service Quick Reference

## Quick Start

### 1. Enable SMS Service
Edit `application.yml`:
```yaml
sms:
  melipayamak:
    enabled: true  # Change from false to true
    username: your-username
    password: your-password
    line-number: 5000xxxxx
```

### 2. Inject SMS Service
```java
@Autowired(required = false)
private SmsService smsService;
```

### 3. Send SMS
```java
if (smsService != null) {
    SmsResult result = smsService.sendSms("09123456789", "سلام!");
    if (result.isSuccess()) {
        log.info("SMS sent: {}", result.getMessageId());
    }
}
```

## API Endpoints

Base URL: `http://localhost:8081/api/sms`

### Check Status
```http
GET /api/sms/status
```

### Send Single SMS
```http
POST /api/sms/send
Content-Type: application/json

{
  "phoneNumber": "09123456789",
  "message": "سلام، این یک پیام تستی است"
}
```

### Send Bulk SMS
```http
POST /api/sms/send/bulk
Content-Type: application/json

{
  "phoneNumbers": ["09123456789", "09987654321"],
  "message": "پیام گروهی"
}
```

### Send Template SMS
```http
POST /api/sms/send/template
Content-Type: application/json

{
  "phoneNumber": "09123456789",
  "templateId": "REMINDER",
  "parameters": {
    "name": "احمد",
    "date": "1403/10/15"
  }
}
```

### Check SMS Status
```http
GET /api/sms/status/{messageId}
```

### Get Account Balance
```http
GET /api/sms/balance
```

### List Templates
```http
GET /api/sms/templates
```

## Available Templates

| Template ID | Parameters | Example |
|------------|------------|---------|
| REMINDER | name, date | `{"name": "احمد", "date": "1403/10/15"}` |
| PAYMENT_SUCCESS | amount | `{"amount": "50000"}` |
| PAYMENT_FAILED | none | `{}` |
| SUBSCRIPTION_CREATED | name | `{"name": "احمد"}` |
| SUBSCRIPTION_EXPIRED | name | `{"name": "احمد"}` |
| SUBSCRIPTION_RENEWED | name, date | `{"name": "احمد", "date": "1403/11/01"}` |

## Code Examples

### Example 1: Send Welcome SMS
```java
@Service
public class CustomerService {
    
    @Autowired(required = false)
    private SmsService smsService;
    
    public void sendWelcomeSms(Customer customer) {
        if (smsService != null) {
            String message = String.format(
                "سلام %s، به سیستم دریافت خوش آمدید!",
                customer.getName()
            );
            smsService.sendSms(customer.getPhoneNumber(), message);
        }
    }
}
```

### Example 2: Send Payment Confirmation
```java
public void notifyPaymentSuccess(Payment payment) {
    if (smsService != null) {
        Map<String, String> params = Map.of(
            "amount", String.valueOf(payment.getAmount())
        );
        smsService.sendTemplateSms(
            payment.getCustomer().getPhoneNumber(),
            "PAYMENT_SUCCESS",
            params
        );
    }
}
```

### Example 3: Send Subscription Reminder
```java
@Scheduled(cron = "0 0 9 * * *")  // Daily at 9 AM
public void sendSubscriptionReminders() {
    if (smsService == null) return;
    
    List<Subscription> expiringSoon = subscriptionService
        .findExpiringSoon(3);  // 3 days
    
    for (Subscription sub : expiringSoon) {
        Map<String, String> params = Map.of(
            "name", sub.getCustomer().getName(),
            "date", sub.getEndDate().toString()
        );
        smsService.sendTemplateSms(
            sub.getCustomer().getPhoneNumber(),
            "REMINDER",
            params
        );
    }
}
```

### Example 4: Check Balance Before Sending
```java
public void sendIfBalanceSufficient(String phone, String message) {
    if (smsService != null) {
        AccountBalance balance = smsService.getBalance();
        if (balance.getBalance() > 1000) {
            smsService.sendSms(phone, message);
        } else {
            log.warn("SMS balance too low: {}", balance.getBalance());
        }
    }
}
```

## Architecture Overview

```
┌─────────────────────────────────────────┐
│         Application Layer               │
│  (Controllers, Services, Schedulers)    │
└───────────────┬─────────────────────────┘
                │
                ↓
┌─────────────────────────────────────────┐
│          SmsService (Facade)            │
│    - Simplified API                     │
│    - Provider abstraction               │
└───────────────┬─────────────────────────┘
                │
                ↓
┌─────────────────────────────────────────┐
│         SmsProvider Interface           │
│    - sendSms()                          │
│    - sendBulkSms()                      │
│    - sendTemplateSms()                  │
│    - checkStatus()                      │
│    - getBalance()                       │
└───────────────┬─────────────────────────┘
                │
                ↓
┌─────────────────────────────────────────┐
│      MeliPayamakProvider                │
│    @ConditionalOnProperty               │
│    (enabled via config)                 │
└───────────────┬─────────────────────────┘
                │
                ↓
┌─────────────────────────────────────────┐
│      MeliPayamak REST API               │
│    https://rest.payamak-panel.com       │
└─────────────────────────────────────────┘
```

## File Structure

```
src/main/java/com/daryaftmanazam/daryaftcore/
├── controller/
│   └── SmsController.java                    # REST API
├── model/
│   ├── Business.java                         # Updated with SmsConfig
│   └── SmsConfig.java                        # Embeddable config
└── service/
    └── sms/
        ├── AccountBalance.java               # Balance DTO
        ├── MeliPayamakBalanceResponse.java   # API response
        ├── MeliPayamakProvider.java          # Implementation
        ├── MeliPayamakResponse.java          # API response
        ├── MeliPayamakStatusResponse.java    # API response
        ├── SmsProvider.java                  # Interface
        ├── SmsResult.java                    # Result DTO
        ├── SmsService.java                   # Facade
        ├── SmsStatus.java                    # Status enum
        └── package-info.java                 # Documentation

src/main/resources/
└── application.yml                           # Configuration
```

## Configuration Properties

| Property | Description | Default | Required |
|----------|-------------|---------|----------|
| `sms.melipayamak.enabled` | Enable/disable SMS service | `false` | Yes |
| `sms.melipayamak.api-key` | MeliPayamak API key | - | Yes |
| `sms.melipayamak.username` | Account username | - | Yes |
| `sms.melipayamak.password` | Account password | - | Yes |
| `sms.melipayamak.line-number` | SMS sender number | - | Yes |

## Troubleshooting

### Problem: "SMS service is not available"
**Solution**: 
1. Check `sms.melipayamak.enabled=true` in application.yml
2. Verify all credentials are set
3. Restart application

### Problem: "No qualifying bean of type 'SmsProvider'"
**Solution**: SMS is disabled by default. Enable it in configuration.

### Problem: SMS not being sent
**Solution**:
1. Check logs for errors
2. Verify phone number format: `09xxxxxxxxx`
3. Check account balance: `GET /api/sms/balance`
4. Test credentials in MeliPayamak panel

### Problem: "Failed to send SMS: Authentication failed"
**Solution**: Verify username and password are correct

## Testing with Swagger

1. Start application: `./mvnw spring-boot:run`
2. Open Swagger UI: http://localhost:8081/swagger-ui.html
3. Navigate to "SMS" section
4. Test endpoints:
   - Check status first
   - Send test SMS
   - Check balance

## Testing with cURL

```bash
# Check status
curl http://localhost:8081/api/sms/status

# Send SMS
curl -X POST http://localhost:8081/api/sms/send \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"09123456789","message":"تست"}'

# Get balance
curl http://localhost:8081/api/sms/balance

# List templates
curl http://localhost:8081/api/sms/templates
```

## Best Practices

1. ✅ Always check if SMS service is available before using
2. ✅ Use `@Autowired(required = false)` for optional injection
3. ✅ Handle SMS failures gracefully
4. ✅ Log all SMS operations
5. ✅ Monitor account balance regularly
6. ✅ Use templates for consistent messaging
7. ✅ Validate phone numbers before sending
8. ✅ Consider async processing for non-critical SMS

## Security Notes

⚠️ **Important Security Considerations:**

1. **Never commit credentials to Git**
   - Use environment variables in production
   - Use Spring profiles for different environments

2. **Protect API endpoints**
   - Add authentication to SMS endpoints
   - Implement rate limiting

3. **Validate input**
   - Validate phone numbers
   - Sanitize message content

4. **Monitor usage**
   - Track SMS costs
   - Set up alerts for unusual activity

## Production Checklist

- [ ] Update credentials in production environment
- [ ] Enable SMS: `sms.melipayamak.enabled=true`
- [ ] Test SMS sending in production
- [ ] Set up balance monitoring
- [ ] Configure logging levels
- [ ] Add authentication to SMS endpoints
- [ ] Implement rate limiting
- [ ] Set up error alerting
- [ ] Document operational procedures
- [ ] Train team on SMS usage

## Support

For issues or questions:
1. Check [SMS_SERVICE_DOCUMENTATION.md](SMS_SERVICE_DOCUMENTATION.md)
2. Review application logs
3. Test with MeliPayamak panel directly
4. Contact development team

---

**Last Updated**: December 23, 2025
