# Rate Limiting Architecture Overview

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT REQUEST                          │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    RateLimitFilter (Global)                     │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  • Intercepts ALL requests                                │ │
│  │  • IP-based tracking (supports X-Forwarded-For)          │ │
│  │  • Limit: 100 requests/minute                            │ │
│  │  • Returns 429 if exceeded                               │ │
│  │  • Adds X-Rate-Limit-Remaining header                    │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security Chain                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Controller Layer                           │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  @RateLimit Annotation Applied                           │ │
│  │  • Login: 5 req/min                                       │ │
│  │  • Register: 3 req/hour                                   │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    RateLimitAspect (AOP)                        │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │  • Intercepts @RateLimit annotated methods               │ │
│  │  • Creates buckets per IP + Method                       │ │
│  │  • Uses configured limits from annotation                │ │
│  │  • Throws RateLimitExceededException if limit exceeded   │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────┬────────────────────────────────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
           ┌─────────────┐   ┌──────────────────┐
           │   SUCCESS   │   │  RATE LIMITED    │
           │   (200)     │   │  (429)           │
           └─────────────┘   └──────────────────┘
                                      │
                                      ▼
                          ┌──────────────────────┐
                          │ GlobalExceptionHandler│
                          │  Returns JSON Error   │
                          └──────────────────────┘
```

## 🔄 Request Flow

### Normal Request Flow
```
1. Client → RateLimitFilter
   └─> Check global bucket (100/min)
       └─> ✅ Token available → Continue

2. Request → Spring Security
   └─> ✅ Authentication/Authorization

3. Request → Controller
   └─> Method with @RateLimit

4. AOP Aspect → RateLimitAspect
   └─> Check method-specific bucket (e.g., 5/min for login)
       └─> ✅ Token available → Execute method

5. Response → Client (200 OK)
```

### Rate Limited Flow
```
1. Client → RateLimitFilter
   └─> Check global bucket
       └─> ❌ No tokens available
           └─> Return 429 immediately
               └─> Response: {error: "محدودیت تعداد درخواست", retryAfter: 60}

OR

1. Client → RateLimitFilter
   └─> ✅ Pass global check

2-3. Through Security and Controller

4. AOP Aspect → RateLimitAspect
   └─> Check method-specific bucket
       └─> ❌ No tokens available
           └─> Throw RateLimitExceededException

5. GlobalExceptionHandler
   └─> Catch exception
       └─> Return 429 with error message
```

## 🪣 Token Bucket Algorithm

```
┌─────────────────────────────────────────────┐
│              Token Bucket                   │
│                                             │
│  Capacity: 100 tokens                       │
│  ┌───────────────────────────────────────┐ │
│  │ ⚪⚪⚪⚪⚪⚪⚪⚪⚪⚪ (10 tokens)        │ │
│  │                                       │ │
│  │ Current: 47 tokens remaining          │ │
│  └───────────────────────────────────────┘ │
│                                             │
│  Refill: +100 tokens every 1 minute        │
│  Consumption: 1 token per request          │
└─────────────────────────────────────────────┘

Timeline:
─────────────────────────────────────────────►
0s      10s     20s     30s     60s (Refill)
│       │       │       │       │
▼       ▼       ▼       ▼       ▼
100→90→80→70→60→50 ... → 100 (Reset)
```

## 📦 Component Interaction

```
┌──────────────────────────────────────────────────────────────┐
│                        Application                           │
│                                                              │
│  ┌────────────────┐         ┌──────────────────────────┐   │
│  │  @RateLimit    │◄────────│  RateLimitAspect         │   │
│  │  Annotation    │         │  (Around Advice)         │   │
│  └────────────────┘         └──────────────────────────┘   │
│         │                              │                    │
│         │                              │                    │
│         ▼                              ▼                    │
│  ┌────────────────┐         ┌──────────────────────────┐   │
│  │ AuthController │         │  Bucket4j               │   │
│  │  - login()     │◄────────│  ConcurrentHashMap      │   │
│  │  - register()  │         │  <IP:Method, Bucket>    │   │
│  └────────────────┘         └──────────────────────────┘   │
│                                                              │
│  ┌────────────────┐         ┌──────────────────────────┐   │
│  │ RateLimitFilter│◄────────│  Bucket4j               │   │
│  │  (OncePerReq)  │         │  ConcurrentHashMap      │   │
│  │                │         │  <IP, Bucket>           │   │
│  └────────────────┘         └──────────────────────────┘   │
│         │                                                    │
│         │                                                    │
│         ▼                                                    │
│  ┌────────────────────────────────────────────────────┐    │
│  │          GlobalExceptionHandler                    │    │
│  │  - handleRateLimitExceeded()                       │    │
│  │  - handleAccessDenied()                            │    │
│  │  - handleValidationExceptions()                    │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

## 🎯 Protection Layers

```
Layer 1: Global Rate Limiting (RateLimitFilter)
├─ Protects entire API surface
├─ 100 requests per minute per IP
└─ First line of defense against DDoS

Layer 2: Method-Level Rate Limiting (RateLimitAspect)
├─ Protects sensitive endpoints
├─ Configurable per method
└─ Granular control (login: 5/min, register: 3/hr)

Layer 3: Input Validation
├─ Bean Validation (@Valid)
├─ Pattern matching
└─ Format checking

Layer 4: Exception Handling
├─ Consistent error responses
├─ Localized messages (Persian)
└─ Appropriate HTTP status codes
```

## 🔑 Key Design Decisions

### 1. Two-Layer Rate Limiting
**Why:** Provides both broad protection and fine-grained control
- Global filter for DDoS protection
- Method-level for endpoint-specific limits

### 2. IP-Based Tracking
**Why:** Simple, effective, and user-agnostic
- Works before authentication
- Protects login/register endpoints
- Supports X-Forwarded-For for proxies

### 3. ConcurrentHashMap Storage
**Why:** Fast, thread-safe, in-memory
- No external dependencies
- Low latency
- Suitable for single-instance deployments

### 4. Token Bucket Algorithm
**Why:** Allows burst traffic while enforcing limits
- More flexible than fixed window
- Better user experience
- Industry standard

## 📈 Scalability Considerations

### Current Setup (Single Instance)
✅ In-memory ConcurrentHashMap
✅ Fast and simple
⚠️ Not shared across instances

### Multi-Instance Setup (Future)
Would need:
- Redis for shared state
- Distributed rate limiting
- Consistent bucket synchronization

### Migration Path:
```java
// Current: In-memory
private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

// Future: Redis-backed
@Autowired
private RedissonClient redisson;
private final ProxyManager<String> proxyManager = ...;
```

## ✅ Implementation Checklist

- [x] Bucket4j dependency added
- [x] RateLimitFilter created
- [x] @RateLimit annotation defined
- [x] RateLimitAspect implemented
- [x] RateLimitExceededException created
- [x] AuthController endpoints protected
- [x] LoginRequest validation enhanced
- [x] RegisterRequest validation enhanced
- [x] ValidationConfig created
- [x] GlobalExceptionHandler updated
- [x] Documentation created
- [x] No compilation errors

## 🎉 Result

Enterprise-grade rate limiting and API security fully implemented with:
- ✅ Brute force protection
- ✅ DDoS mitigation
- ✅ Input validation
- ✅ Comprehensive error handling
- ✅ Persian localization
- ✅ Production-ready code
