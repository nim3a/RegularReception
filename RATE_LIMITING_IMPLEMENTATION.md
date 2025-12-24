# Rate Limiting and API Security Implementation

## Overview
Comprehensive rate limiting and API security implementation using Bucket4j for the Spring Boot application. This protects against brute force and DDoS attacks with configurable limits for different endpoints.

## ✅ Implemented Components

### 1. **Bucket4j Dependency** - `pom.xml`
Added Bucket4j 8.7.0 for token bucket rate limiting algorithm.

### 2. **RateLimitFilter** - Global Rate Limiting
**File:** `src/main/java/com/daryaftmanazam/daryaftcore/security/RateLimitFilter.java`

**Features:**
- Global IP-based rate limiting for all API requests
- Default: 100 requests per minute per IP
- Token bucket algorithm implementation
- X-Forwarded-For header support for proxy scenarios
- Returns HTTP 429 with retry-after headers when limit exceeded
- Persian error messages

**Headers:**
- `X-Rate-Limit-Remaining`: Number of remaining tokens
- `X-Rate-Limit-Retry-After-Seconds`: Seconds to wait before retry

### 3. **@RateLimit Annotation** - Method-Level Configuration
**File:** `src/main/java/com/daryaftmanazam/daryaftcore/annotation/RateLimit.java`

**Configuration Options:**
- `capacity`: Maximum number of tokens (default: 10)
- `refillTokens`: Number of tokens to refill (default: 10)
- `refillDuration`: Duration for refill (default: 1)
- `refillUnit`: Time unit (default: MINUTES)

**Example Usage:**
```java
@RateLimit(capacity = 5, refillTokens = 5, refillDuration = 1, refillUnit = TimeUnit.MINUTES)
```

### 4. **RateLimitAspect** - Method-Level Enforcement
**File:** `src/main/java/com/daryaftmanazam/daryaftcore/aspect/RateLimitAspect.java`

**Features:**
- AOP-based rate limiting for annotated methods
- Separate buckets per IP + method combination
- Configurable rate limits per method
- Throws `RateLimitExceededException` when limit exceeded

### 5. **RateLimitExceededException**
**File:** `src/main/java/com/daryaftmanazam/daryaftcore/exception/RateLimitExceededException.java`

Returns HTTP 429 status with Persian error message.

### 6. **Protected Endpoints** - AuthController

#### Login Endpoint
```java
@RateLimit(capacity = 5, refillTokens = 5, refillDuration = 1, refillUnit = TimeUnit.MINUTES)
@PostMapping("/login")
```
**Limit:** 5 login attempts per minute per IP

#### Register Endpoint
```java
@RateLimit(capacity = 3, refillTokens = 3, refillDuration = 1, refillUnit = TimeUnit.HOURS)
@PostMapping("/register")
```
**Limit:** 3 registrations per hour per IP

### 7. **Enhanced Input Validation**

#### ValidationConfig
**File:** `src/main/java/com/daryaftmanazam/daryaftcore/config/ValidationConfig.java`

Provides validator bean for manual validation when needed.

#### LoginRequest Validation
- Username: 3-50 characters, required
- Password: Minimum 6 characters, required

#### RegisterRequest Validation
- Username: 3-50 characters, alphanumeric + underscore only
- Email: Valid email format
- Password: Minimum 8 characters, must contain letters and numbers

### 8. **Global Exception Handler** - Enhanced

**New Handlers Added:**
- `RateLimitExceededException`: HTTP 429 with retry message
- `AccessDeniedException`: HTTP 403 for unauthorized access
- Enhanced validation error handling with Persian messages

## Security Features

### ✅ Protection Against:
1. **Brute Force Attacks**
   - Login rate limiting (5 attempts/minute)
   - Registration throttling (3 attempts/hour)

2. **DDoS Attacks**
   - Global request rate limiting (100 req/min)
   - IP-based tracking

3. **Invalid Input**
   - Comprehensive validation on all inputs
   - Pattern matching for usernames and passwords
   - Email format validation

### ✅ Rate Limiting Strategies:

**Two-Layer Approach:**
1. **Global Filter**: Applies to all requests (100/min)
2. **Method-Level**: Specific limits for sensitive endpoints

**Token Bucket Algorithm:**
- Capacity: Maximum tokens available
- Refill: Tokens added at configured intervals
- Consumption: 1 token per request

## Configuration

### Default Limits:
```
Global: 100 requests/minute
Login: 5 attempts/minute
Register: 3 attempts/hour
```

### Customization:
To apply rate limiting to any endpoint:
```java
@RateLimit(capacity = 10, refillTokens = 10, refillDuration = 5, refillUnit = TimeUnit.MINUTES)
public ResponseEntity<?> yourMethod() {
    // ...
}
```

## API Responses

### Rate Limit Exceeded (429)
```json
{
  "error": "محدودیت تعداد درخواست",
  "retryAfter": 60
}
```

### Validation Error (400)
```json
{
  "errors": {
    "username": "نام کاربری باید بین 3 تا 50 کاراکتر باشد",
    "password": "رمز عبور باید شامل حروف و اعداد باشد"
  }
}
```

### Access Denied (403)
```json
{
  "error": "دسترسی به این منبع مجاز نیست"
}
```

## Testing

### Test Rate Limiting:
```bash
# Test global rate limit
for i in {1..150}; do curl http://localhost:8080/api/endpoint; done

# Test login rate limit
for i in {1..10}; do 
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}'; 
done
```

## Advantages

1. **Dual-Layer Protection**: Global + method-level rate limiting
2. **Configurable**: Easy to adjust limits per endpoint
3. **IP-Based Tracking**: Prevents abuse from single sources
4. **Proxy Support**: Handles X-Forwarded-For headers
5. **Clear Feedback**: Informative error messages and headers
6. **Persian Localization**: All messages in Persian
7. **Memory-Efficient**: ConcurrentHashMap for bucket storage
8. **Non-Blocking**: Uses token bucket algorithm

## Future Enhancements (Optional)

### Redis-Based Distributed Rate Limiting
For multi-instance deployments:
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-redis</artifactId>
</dependency>
```

### Rate Limit by User ID
Instead of just IP-based:
```java
private String getKey(HttpServletRequest request, ProceedingJoinPoint pjp) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = auth != null ? auth.getName() : request.getRemoteAddr();
    return userId + ":" + pjp.getSignature().getName();
}
```

## Monitoring Recommendations

1. **Log rate limit violations** (already implemented)
2. **Track bucket usage metrics**
3. **Alert on excessive violations**
4. **Adjust limits based on traffic patterns**

## Summary

✅ **Bucket4j dependency added**
✅ **Global rate limiting filter implemented**
✅ **Method-level rate limiting with annotations**
✅ **AuthController endpoints protected**
✅ **Enhanced input validation**
✅ **Comprehensive exception handling**
✅ **Persian error messages**
✅ **IP-based tracking with proxy support**

Your Spring Boot application now has enterprise-grade rate limiting and API security measures in place!
