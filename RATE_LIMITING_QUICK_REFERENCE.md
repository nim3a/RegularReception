# Rate Limiting Quick Reference

## 🚀 Quick Start

### Apply Rate Limiting to Any Endpoint
```java
@RateLimit(capacity = 10, refillTokens = 10, refillDuration = 1, refillUnit = TimeUnit.MINUTES)
@PostMapping("/your-endpoint")
public ResponseEntity<?> yourMethod() {
    // Your code
}
```

## 📊 Current Configuration

| Endpoint | Limit | Window | Status |
|----------|-------|--------|--------|
| Global (All APIs) | 100 requests | 1 minute | ✅ Active |
| POST /api/auth/login | 5 requests | 1 minute | ✅ Active |
| POST /api/auth/register | 3 requests | 1 hour | ✅ Active |

## 🔧 Configuration Options

```java
@RateLimit(
    capacity = 10,              // Max tokens in bucket
    refillTokens = 10,         // Tokens added per refill
    refillDuration = 1,        // Refill interval
    refillUnit = TimeUnit.MINUTES  // SECONDS, MINUTES, HOURS, DAYS
)
```

## 📝 Common Rate Limit Patterns

### High Security (Login, Password Reset)
```java
@RateLimit(capacity = 5, refillTokens = 5, refillDuration = 1, refillUnit = TimeUnit.MINUTES)
```

### Medium Security (Registration, Profile Updates)
```java
@RateLimit(capacity = 3, refillTokens = 3, refillDuration = 1, refillUnit = TimeUnit.HOURS)
```

### API Endpoints (General Use)
```java
@RateLimit(capacity = 50, refillTokens = 50, refillDuration = 1, refillUnit = TimeUnit.MINUTES)
```

### Heavy Operations (Reports, Exports)
```java
@RateLimit(capacity = 5, refillTokens = 5, refillDuration = 1, refillUnit = TimeUnit.HOURS)
```

## 🔍 Response Headers

### Success Response
```
HTTP/1.1 200 OK
X-Rate-Limit-Remaining: 47
```

### Rate Limit Exceeded
```
HTTP/1.1 429 Too Many Requests
X-Rate-Limit-Retry-After-Seconds: 60
Content-Type: application/json

{
  "error": "محدودیت تعداد درخواست",
  "retryAfter": 60
}
```

## 🛡️ Validation Rules

### LoginRequest
- ✅ Username: 3-50 chars, required
- ✅ Password: 6+ chars, required

### RegisterRequest
- ✅ Username: 3-50 chars, alphanumeric + underscore
- ✅ Email: Valid format
- ✅ Password: 8+ chars, letters + numbers

## 🧪 Testing Commands

### Test Global Rate Limit
```bash
# Send 150 requests rapidly
for i in {1..150}; do 
  curl http://localhost:8080/api/auth/me
done
```

### Test Login Rate Limit
```bash
# Send 10 login requests
for i in {1..10}; do 
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"password"}'
done
```

### Test Register Rate Limit
```bash
# Send 5 registration requests
for i in {1..5}; do 
  curl -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"user'$i'","email":"user'$i'@test.com","password":"Password123"}'
done
```

## 📦 Files Modified/Created

### Created Files
- ✅ `RateLimitFilter.java` - Global filter
- ✅ `RateLimit.java` - Annotation
- ✅ `RateLimitAspect.java` - AOP aspect
- ✅ `RateLimitExceededException.java` - Exception
- ✅ `ValidationConfig.java` - Validation setup

### Modified Files
- ✅ `pom.xml` - Added Bucket4j dependency
- ✅ `AuthController.java` - Applied rate limiting
- ✅ `LoginRequest.java` - Enhanced validation
- ✅ `RegisterRequest.java` - Enhanced validation
- ✅ `GlobalExceptionHandler.java` - Added handlers

## 🎯 Key Features

✅ IP-based rate limiting
✅ Method-level configuration
✅ Token bucket algorithm
✅ Proxy support (X-Forwarded-For)
✅ Persian error messages
✅ Comprehensive validation
✅ Global + endpoint-specific limits

## 🔗 Related Documentation

- Full details: [RATE_LIMITING_IMPLEMENTATION.md](RATE_LIMITING_IMPLEMENTATION.md)
- Exception handling: [EXCEPTION_HANDLING.md](EXCEPTION_HANDLING.md)
- API reference: [API_QUICK_REFERENCE.md](API_QUICK_REFERENCE.md)
