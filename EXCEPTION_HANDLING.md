# Exception Handling Documentation

## Overview

This document describes the comprehensive exception handling implementation for the Daryaft Core application.

## Custom Exceptions

All custom exceptions extend `RuntimeException` and are located in the `com.daryaftmanazam.daryaftcore.exception` package.

### Not Found Exceptions (404)

| Exception | Description | Example Usage |
|-----------|-------------|---------------|
| `BusinessNotFoundException` | Business entity not found | `throw new BusinessNotFoundException(id);` |
| `CustomerNotFoundException` | Customer entity not found | `throw new CustomerNotFoundException(id);` |
| `PaymentPlanNotFoundException` | Payment plan not found | `throw new PaymentPlanNotFoundException(id);` |
| `SubscriptionNotFoundException` | Subscription not found | `throw new SubscriptionNotFoundException(id);` |
| `PaymentNotFoundException` | Payment not found | `throw new PaymentNotFoundException(id);` |

### Bad Request Exceptions (400)

| Exception | Description | Example Usage |
|-----------|-------------|---------------|
| `InvalidSubscriptionException` | Invalid subscription | `throw new InvalidSubscriptionException("reason");` |
| `InvalidPaymentException` | Invalid payment | `throw new InvalidPaymentException("reason");` |
| `PaymentProcessingException` | Payment processing failed | `throw new PaymentProcessingException(paymentId, "reason");` |
| `InvalidOperationException` | Invalid operation attempted | `throw new InvalidOperationException("operation", "reason");` |
| `ValidationException` | Validation failed | `throw new ValidationException("message", fieldErrors);` |

## Global Exception Handler

The `GlobalExceptionHandler` class uses `@RestControllerAdvice` to handle all exceptions globally.

### Features

1. **Persian Error Messages**: All error messages are retrieved from `messages_fa_IR.properties`
2. **Consistent Error Structure**: All responses use `ErrorResponse` DTO
3. **Proper Logging**: Exceptions are logged at appropriate levels (ERROR, WARN)
4. **HTTP Status Codes**: Returns correct status codes based on exception type
5. **Request Context**: Includes request path and timestamp in error responses

### Handled Exceptions

| Exception Type | HTTP Status | Error Code | Log Level |
|----------------|-------------|------------|-----------|
| `BusinessNotFoundException` | 404 | BUSINESS_NOT_FOUND | WARN |
| `CustomerNotFoundException` | 404 | CUSTOMER_NOT_FOUND | WARN |
| `PaymentPlanNotFoundException` | 404 | PAYMENT_PLAN_NOT_FOUND | WARN |
| `SubscriptionNotFoundException` | 404 | SUBSCRIPTION_NOT_FOUND | WARN |
| `PaymentNotFoundException` | 404 | PAYMENT_NOT_FOUND | WARN |
| `PaymentProcessingException` | 400 | PAYMENT_PROCESSING_FAILED | ERROR |
| `InvalidOperationException` | 400 | INVALID_OPERATION | WARN |
| `ValidationException` | 400 | VALIDATION_FAILED | WARN |
| `InvalidSubscriptionException` | 400 | INVALID_SUBSCRIPTION | WARN |
| `InvalidPaymentException` | 400 | INVALID_PAYMENT | WARN |
| `MethodArgumentNotValidException` | 400 | VALIDATION_FAILED | WARN |
| `DataIntegrityViolationException` | 409 | DATA_CONFLICT | ERROR |
| `MethodArgumentTypeMismatchException` | 400 | TYPE_MISMATCH | WARN |
| `IllegalArgumentException` | 400 | ILLEGAL_ARGUMENT | WARN |
| `Exception` (all others) | 500 | INTERNAL_SERVER_ERROR | ERROR |

## Error Response Structure

All error responses follow this structure:

```json
{
  "status": 404,
  "error_code": "BUSINESS_NOT_FOUND",
  "message": "کسب‌وکار یافت نشد",
  "path": "/api/businesses/123",
  "timestamp": "2025-12-22T10:30:45",
  "field_errors": {
    "email": "Invalid email format",
    "name": "Name is required"
  }
}
```

### Fields

- **status** (Integer): HTTP status code
- **error_code** (String): Unique error code identifier
- **message** (String): Persian error message
- **path** (String): Request URI that caused the error
- **timestamp** (LocalDateTime): When the error occurred
- **field_errors** (Map): Field-level validation errors (optional)
- **details** (List): Additional error details (optional)

## Persian Error Messages

All error messages are defined in `src/main/resources/messages_fa_IR.properties`:

```properties
# Not Found Errors (404)
error.business.notfound=کسب‌وکار یافت نشد
error.customer.notfound=مشتری یافت نشد
error.paymentplan.notfound=طرح پرداخت یافت نشد
error.subscription.notfound=اشتراک یافت نشد
error.payment.notfound=پرداخت یافت نشد

# Bad Request Errors (400)
error.subscription.invalid=اشتراک نامعتبر است
error.payment.invalid=پرداخت نامعتبر است
error.payment.processing=پردازش پرداخت با خطا مواجه شد
error.operation.invalid=عملیات نامعتبر است
error.validation.failed=اعتبارسنجی ناموفق بود
error.type.mismatch=مقدار '%s' برای پارامتر '%s' نامعتبر است
error.argument.illegal=آرگومان نامعتبر است

# Conflict Errors (409)
error.data.conflict=تضاد در داده‌ها
error.data.duplicate=رکورد تکراری است
error.data.reference=نقض محدودیت مرجع

# Internal Server Error (500)
error.internal=خطای داخلی سرور. لطفاً با پشتیبانی تماس بگیرید
```

## Usage Examples

### 1. Throwing Not Found Exception

```java
@Service
public class BusinessService {
    public BusinessResponseDto getBusinessById(Long id) {
        return businessRepository.findById(id)
            .map(businessMapper::toDto)
            .orElseThrow(() -> new BusinessNotFoundException(id));
    }
}
```

### 2. Throwing Validation Exception

```java
@Service
public class SubscriptionService {
    public void createSubscription(SubscriptionRequestDto request) {
        if (!isValid(request)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("startDate", "Start date must be in the future");
            throw new ValidationException("Validation failed", errors);
        }
    }
}
```

### 3. Throwing Payment Processing Exception

```java
@Service
public class PaymentService {
    public void processPayment(Long paymentId) {
        try {
            // Process payment logic
        } catch (Exception e) {
            throw new PaymentProcessingException(paymentId, "Payment gateway error");
        }
    }
}
```

### 4. Handling Data Integrity Violations

The `GlobalExceptionHandler` automatically handles database constraint violations:

```java
@Service
public class CustomerService {
    public void createCustomer(CustomerRequestDto request) {
        // If email is duplicate, DataIntegrityViolationException is thrown
        // and automatically handled by GlobalExceptionHandler
        customerRepository.save(customer);
    }
}
```

## Testing

### Unit Tests

Unit tests for `GlobalExceptionHandler` are in:
- `src/test/java/com/daryaftmanazam/daryaftcore/exception/GlobalExceptionHandlerTest.java`

These tests verify:
- Correct HTTP status codes
- Proper error response structure
- Error codes are set correctly
- Persian messages are loaded

### Integration Tests

Integration tests for exception handling through REST endpoints are in:
- `src/test/java/com/daryaftmanazam/daryaftcore/exception/ExceptionHandlingIntegrationTest.java`

These tests verify:
- End-to-end exception handling
- Validation error responses
- 404 errors for non-existent resources
- Persian messages in actual HTTP responses
- Consistent error response structure

### Running Tests

```bash
# Run all exception tests
./mvnw test -Dtest=GlobalExceptionHandlerTest

# Run integration tests
./mvnw test -Dtest=ExceptionHandlingIntegrationTest

# Run all tests
./mvnw test
```

## Best Practices

1. **Always use custom exceptions** instead of generic RuntimeException
2. **Provide meaningful error messages** that help with debugging
3. **Include entity IDs** in exception messages when applicable
4. **Use ValidationException** for complex validation errors with field-level details
5. **Let GlobalExceptionHandler handle all exceptions** - don't catch and return error responses in controllers
6. **Log exceptions at appropriate levels**:
   - ERROR: For unexpected errors and system failures
   - WARN: For expected business logic exceptions
7. **Don't expose sensitive information** in error messages (stack traces, database details)

## Error Codes Reference

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| BUSINESS_NOT_FOUND | 404 | Business entity not found |
| CUSTOMER_NOT_FOUND | 404 | Customer entity not found |
| PAYMENT_PLAN_NOT_FOUND | 404 | Payment plan not found |
| SUBSCRIPTION_NOT_FOUND | 404 | Subscription not found |
| PAYMENT_NOT_FOUND | 404 | Payment not found |
| INVALID_SUBSCRIPTION | 400 | Subscription validation failed |
| INVALID_PAYMENT | 400 | Payment validation failed |
| PAYMENT_PROCESSING_FAILED | 400 | Payment processing error |
| INVALID_OPERATION | 400 | Invalid operation attempted |
| VALIDATION_FAILED | 400 | Request validation failed |
| TYPE_MISMATCH | 400 | Parameter type mismatch |
| ILLEGAL_ARGUMENT | 400 | Illegal argument provided |
| DATA_CONFLICT | 409 | Database constraint violation |
| INTERNAL_SERVER_ERROR | 500 | Unexpected server error |

## Monitoring and Logging

All exceptions are logged with:
- Exception type and message
- Request URI (when available)
- Stack trace (for ERROR level)
- Timestamp

Log format example:
```
WARN  - Business not found: Business not found with id: 123
ERROR - Unexpected exception occurred: 
java.lang.RuntimeException: Database connection failed
    at com.daryaftmanazam...
```

## Future Enhancements

1. Add exception metrics/monitoring integration
2. Implement rate limiting for repeated errors
3. Add support for multiple languages
4. Create custom error pages for web interface
5. Add error correlation IDs for distributed tracing
