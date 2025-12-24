# SMS Service - Getting Started

## 🚀 Quick Start (3 Steps)

### Step 1: Configure Credentials
Edit `src/main/resources/application.yml`:
```yaml
sms:
  melipayamak:
    enabled: true  # ⬅️ Change this to true
    api-key: c2d0e69c-2d62-488c-82ee-16180dd56c1b
    username: YOUR_USERNAME_HERE  # ⬅️ Add your username
    password: YOUR_PASSWORD_HERE  # ⬅️ Add your password
    line-number: YOUR_LINE_NUMBER_HERE  # ⬅️ Add your line number (e.g., 5000xxxxx)
```

### Step 2: Restart Application
```bash
./mvnw spring-boot:run
```

### Step 3: Test SMS
Open Swagger UI: http://localhost:8081/swagger-ui.html
- Navigate to "SMS" section
- Try "GET /api/sms/status" to verify SMS is active
- Try "POST /api/sms/send" to send a test SMS

## 📁 What Was Implemented

### ✅ Core Components
- **SmsProvider Interface** - Abstraction for multiple providers
- **MeliPayamakProvider** - Complete MeliPayamak integration
- **SmsService** - Easy-to-use facade
- **SmsController** - REST API with 7 endpoints
- **SmsConfig Entity** - Business-level SMS configuration

### ✅ Features
- Send single SMS
- Send bulk SMS
- Send template SMS (6 built-in templates)
- Check delivery status
- Check account balance
- Config-based activation (disabled by default)

## 📚 Documentation Files

1. **SMS_IMPLEMENTATION_SUMMARY.md** - Start here! Complete overview
2. **SMS_SERVICE_DOCUMENTATION.md** - Comprehensive documentation (460+ lines)
3. **SMS_QUICK_REFERENCE.md** - Quick reference guide (400+ lines)
4. **SMS_README.md** - This file

## 💻 Usage Example

```java
@Service
public class YourService {
    
    @Autowired(required = false)
    private SmsService smsService;
    
    public void sendWelcomeSms(String phoneNumber, String name) {
        if (smsService != null && smsService.isAvailable()) {
            String message = "سلام " + name + "، خوش آمدید!";
            SmsResult result = smsService.sendSms(phoneNumber, message);
            
            if (result.isSuccess()) {
                log.info("SMS sent successfully!");
            }
        }
    }
}
```

## 🌐 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/sms/status` | GET | Check if SMS service is active |
| `/api/sms/send` | POST | Send single SMS |
| `/api/sms/send/bulk` | POST | Send bulk SMS |
| `/api/sms/send/template` | POST | Send template SMS |
| `/api/sms/status/{messageId}` | GET | Check SMS delivery status |
| `/api/sms/balance` | GET | Get account balance |
| `/api/sms/templates` | GET | List available templates |

## 📋 Built-in Templates

| Template ID | Message (Persian) |
|------------|-------------------|
| REMINDER | مشتری گرامی {name}، اشتراک شما تا {date} اعتبار دارد. |
| PAYMENT_SUCCESS | پرداخت شما به مبلغ {amount} تومان با موفقیت انجام شد. |
| PAYMENT_FAILED | پرداخت شما ناموفق بود. لطفاً مجدداً تلاش کنید. |
| SUBSCRIPTION_CREATED | مشتری گرامی {name}، اشتراک شما با موفقیت ایجاد شد. |
| SUBSCRIPTION_EXPIRED | مشتری گرامی {name}، اشتراک شما منقضی شده است. |
| SUBSCRIPTION_RENEWED | مشتری گرامی {name}، اشتراک شما تا {date} تمدید شد. |

## 🧪 Testing

### Using cURL
```bash
# Check status
curl http://localhost:8081/api/sms/status

# Send SMS
curl -X POST http://localhost:8081/api/sms/send \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"09123456789","message":"تست"}'

# Get balance
curl http://localhost:8081/api/sms/balance
```

### Using Swagger UI
1. Open: http://localhost:8081/swagger-ui.html
2. Navigate to "SMS" section
3. Click "Try it out" on any endpoint
4. Fill in the parameters
5. Click "Execute"

## ⚠️ Important Notes

1. **SMS is DISABLED by default** - You must set `enabled: true` in configuration
2. **Requires valid credentials** - Get them from https://panel.melipayamak.com
3. **Optional dependency** - Use `@Autowired(required = false)` when injecting SmsService
4. **Phone number format** - Use Iranian format: `09xxxxxxxxx` (11 digits)

## 🔧 Troubleshooting

### Problem: SMS service not available
**Solution**: Set `sms.melipayamak.enabled: true` in application.yml and restart

### Problem: Authentication failed
**Solution**: Verify username and password are correct

### Problem: SMS not being sent
**Solution**: 
1. Check logs for errors
2. Verify phone number format
3. Check account balance
4. Test credentials in MeliPayamak panel

## 📖 More Information

For detailed documentation, see:
- **SMS_IMPLEMENTATION_SUMMARY.md** - Complete implementation overview
- **SMS_SERVICE_DOCUMENTATION.md** - Comprehensive documentation
- **SMS_QUICK_REFERENCE.md** - API reference and examples

## 🆘 Support

If you encounter issues:
1. Check the documentation files
2. Review application logs
3. Verify configuration settings
4. Test with MeliPayamak panel directly

---

**Ready to use!** Just configure your credentials and enable SMS in application.yml 🚀
