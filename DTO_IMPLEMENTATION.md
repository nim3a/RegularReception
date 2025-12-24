# DTO Classes Implementation Summary

## Overview
Created a comprehensive set of Data Transfer Object (DTO) classes for API communication in your Spring Boot application. All DTOs follow best practices with validation, JSON serialization, and proper mapping utilities.

## Structure

### 1. Request DTOs (`dto.request` package)
Used for receiving data from API clients with validation annotations:

- **BusinessRequest**: Business creation/update with fields:
  - businessName, ownerName, contactEmail, contactPhone, description
  - Validations: @NotBlank, @Email, @Pattern (Iranian phone format)

- **CustomerRequest**: Customer creation/update with fields:
  - firstName, lastName, phoneNumber, email, customerType
  - Validations: @NotBlank, @Email, @Pattern, @NotNull

- **PaymentPlanRequest**: Payment plan creation/update with fields:
  - planName, periodType, periodCount, baseAmount, discountPercentage, lateFeePerDay, gracePeriodDays
  - Validations: @NotBlank, @NotNull, @Min, @DecimalMin, @DecimalMax

- **SubscriptionRequest**: Subscription creation with fields:
  - customerId, paymentPlanId, startDate
  - Validations: @NotNull
  - Date format: yyyy-MM-dd

- **PaymentRequest**: Payment creation with fields:
  - subscriptionId, amount, paymentMethod
  - Validations: @NotNull, @DecimalMin, @NotBlank

### 2. Response DTOs (`dto.response` package)
Used for sending data to API clients with computed fields:

- **BusinessResponse**: Includes all business fields plus:
  - customerCount (computed)
  - activeSubscriptionsCount (computed)

- **CustomerResponse**: Includes all customer fields plus:
  - fullName (computed)
  - businessId, businessName
  - currentSubscriptionStatus, hasActiveSubscription, totalSubscriptions

- **PaymentPlanResponse**: Includes all payment plan fields plus:
  - formattedAmount (formatted with Persian locale)
  - finalAmount (after discount calculation)
  - formattedFinalAmount

- **SubscriptionResponse**: Includes subscription fields plus:
  - customerName, customerPhone
  - planName, periodType, periodCount
  - totalPaid, remainingBalance, paymentCount
  - isOverdue, daysUntilNextPayment

- **PaymentResponse**: Includes all payment fields plus:
  - customerName, planName
  - totalAmount (amount + late fee)
  - formattedPaymentDate, formattedDueDate
  - isLate, daysLate

- **DashboardResponse**: Comprehensive dashboard statistics:
  - Financial metrics: totalRevenue, monthlyRevenue, pendingPayments, overdueAmount
  - Subscription metrics: active, pending, overdue, cancelled counts
  - Customer metrics: total, active, new this month, VIP counts
  - Payment metrics: total, completed, failed, this month counts
  - Growth metrics: revenue, customer, subscription growth percentages

### 3. Common DTOs (`dto` package)
Generic wrapper classes for API responses:

- **ApiResponse<T>**: Generic API response wrapper
  - Fields: success, message, data, timestamp
  - Static methods: success(), error() with various overloads
  - Automatically sets timestamp

- **PageResponse<T>**: Paginated response wrapper
  - Fields: content, pageNumber, pageSize, totalElements, totalPages
  - Additional fields: isFirst, isLast, hasNext, hasPrevious
  - Static method: of(Page<T>) for Spring Data Page conversion
  - Static method: empty() for empty page

- **ErrorResponse**: Error details wrapper
  - Fields: errorCode, message, details, fieldErrors, timestamp, path
  - Static methods: of() with various overloads for different error scenarios

### 4. Mapper Utilities (`util.mapper` package)
Utility classes for converting between entities and DTOs:

- **BusinessMapper**: 
  - toEntity(BusinessRequest) → Business
  - updateEntity(BusinessRequest, Business)
  - toResponse(Business) → BusinessResponse (with computed fields)

- **CustomerMapper**:
  - toEntity(CustomerRequest) → Customer
  - updateEntity(CustomerRequest, Customer)
  - toResponse(Customer) → CustomerResponse (with subscription status)

- **PaymentPlanMapper**:
  - toEntity(PaymentPlanRequest) → PaymentPlan
  - updateEntity(PaymentPlanRequest, PaymentPlan)
  - toResponse(PaymentPlan) → PaymentPlanResponse (with calculated final amount)

- **SubscriptionMapper**:
  - toEntity(SubscriptionRequest) → Subscription
  - toResponse(Subscription) → SubscriptionResponse (with payment statistics)

- **PaymentMapper**:
  - toEntity(PaymentRequest) → Payment
  - toResponse(Payment) → PaymentResponse (with formatted dates and late calculations)

## Key Features

### Validation Annotations
All request DTOs use Jakarta validation annotations:
- @NotNull, @NotBlank for required fields
- @Email for email validation
- @Pattern for phone number format (Iranian: 09xxxxxxxxx)
- @Size for string length constraints
- @Min, @Max for numeric ranges
- @DecimalMin, @DecimalMax for decimal values

### Jackson Annotations
All DTOs use Jackson for JSON serialization:
- @JsonProperty for field name mapping (snake_case)
- @JsonFormat for date/time formatting (ISO format)
- Dates: yyyy-MM-dd
- DateTimes: yyyy-MM-dd'T'HH:mm:ss

### Lombok Integration
All DTOs use Lombok for boilerplate reduction:
- @Data for getters/setters/toString/equals/hashCode
- @Builder for builder pattern
- @NoArgsConstructor and @AllArgsConstructor for constructors

### Computed Fields
Response DTOs include computed fields calculated from entity relationships:
- Customer counts, subscription counts
- Payment statistics (total paid, remaining balance)
- Status indicators (isOverdue, hasActiveSubscription)
- Formatted amounts and dates

## Usage Examples

### Creating a Request
```java
CustomerRequest request = CustomerRequest.builder()
    .firstName("علی")
    .lastName("احمدی")
    .phoneNumber("09123456789")
    .email("ali@example.com")
    .customerType(CustomerType.REGULAR)
    .build();
```

### Converting to Entity
```java
Customer customer = CustomerMapper.toEntity(request);
customer.setBusiness(business); // Set relationship
customerRepository.save(customer);
```

### Creating a Response
```java
Customer customer = customerRepository.findById(id).orElseThrow();
CustomerResponse response = CustomerMapper.toResponse(customer);
return ApiResponse.success(response);
```

### Paginated Response
```java
Page<Customer> page = customerRepository.findAll(pageable);
PageResponse<CustomerResponse> pageResponse = PageResponse.of(
    page.map(CustomerMapper::toResponse)
);
return ApiResponse.success(pageResponse);
```

### Error Response
```java
try {
    // operation
} catch (Exception e) {
    ErrorResponse error = ErrorResponse.of("ERR_001", e.getMessage());
    return ApiResponse.error("Operation failed", error);
}
```

## Next Steps

1. **Create Service Layer**: Implement service classes that use these DTOs and mappers
2. **Create Controller Layer**: Implement REST controllers that accept Request DTOs and return Response DTOs
3. **Exception Handling**: Create @ControllerAdvice for global exception handling using ErrorResponse
4. **Validation**: Ensure @Valid annotation is used in controllers to trigger validation
5. **Testing**: Write unit tests for mappers and integration tests for API endpoints

## Files Created

### Request DTOs (6 files)
- src/main/java/com/daryaftmanazam/daryaftcore/dto/request/BusinessRequest.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/request/CustomerRequest.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/request/PaymentPlanRequest.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/request/SubscriptionRequest.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/request/PaymentRequest.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/request/package-info.java

### Response DTOs (7 files)
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/BusinessResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/CustomerResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/PaymentPlanResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/SubscriptionResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/PaymentResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/DashboardResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/response/package-info.java

### Common DTOs (3 files)
- src/main/java/com/daryaftmanazam/daryaftcore/dto/ApiResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/PageResponse.java
- src/main/java/com/daryaftmanazam/daryaftcore/dto/ErrorResponse.java

### Mappers (6 files)
- src/main/java/com/daryaftmanazam/daryaftcore/util/mapper/BusinessMapper.java
- src/main/java/com/daryaftmanazam/daryaftcore/util/mapper/CustomerMapper.java
- src/main/java/com/daryaftmanazam/daryaftcore/util/mapper/PaymentPlanMapper.java
- src/main/java/com/daryaftmanazam/daryaftcore/util/mapper/SubscriptionMapper.java
- src/main/java/com/daryaftmanazam/daryaftcore/util/mapper/PaymentMapper.java
- src/main/java/com/daryaftmanazam/daryaftcore/util/mapper/package-info.java

**Total: 22 files created**

## Build Status
✅ Project compiled successfully with no errors or warnings.
