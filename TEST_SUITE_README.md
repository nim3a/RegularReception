# RegularReception Test Suite Documentation

## Overview

Comprehensive integration test suite for the RegularReception Spring Boot application covering:
- REST API endpoints
- JWT authentication & authorization
- Multi-tenant data isolation
- Database operations
- Business logic validation
- End-to-end workflows

## Test Structure

```
src/test/java/com/daryaftmanazam/daryaftcore/
├── BaseIntegrationTest.java              # Base test configuration with TestContainers
├── controller/
│   ├── CustomerControllerIntegrationTest.java
│   ├── SubscriptionControllerIntegrationTest.java
│   └── PaymentControllerIntegrationTest.java
├── service/
│   ├── SubscriptionServiceTest.java
│   └── SmsServiceTest.java
├── repository/
│   └── SubscriptionRepositoryIntegrationTest.java
├── security/
│   └── JwtUtilTest.java
└── integration/
    └── SubscriptionWorkflowIntegrationTest.java
```

## Technologies Used

- **Spring Boot Test**: Testing framework
- **TestContainers**: PostgreSQL container for integration tests
- **MockMvc**: REST API testing
- **Mockito**: Service layer mocking
- **AssertJ**: Fluent assertions
- **Spring Security Test**: Authentication testing

## Prerequisites

- Java 23
- Maven 3.6+
- Docker (for TestContainers)
- PostgreSQL driver

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CustomerControllerIntegrationTest
mvn test -Dtest=SubscriptionServiceTest
mvn test -Dtest=JwtUtilTest
```

### Run Tests with Coverage
```bash
mvn clean verify
```

### Run Integration Tests Only
```bash
mvn test -Dgroups=integration
```

### Run Unit Tests Only
```bash
mvn test -Dgroups=unit
```

## Test Configuration

### application-test.yml

Located at `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15-alpine:///testdb
  jpa:
    hibernate:
      ddl-auto: create-drop

jwt:
  secret: [base64-encoded-test-secret]
  expiration: 3600000

sms:
  enabled: false

scheduler:
  enabled: false
```

## Test Categories

### 1. Controller Integration Tests

**CustomerControllerIntegrationTest**
- ✅ Create customer with valid data
- ✅ Validate authentication requirements
- ✅ Test multi-tenant isolation
- ✅ Update and delete operations
- ✅ Search and pagination

**SubscriptionControllerIntegrationTest**
- ✅ Create subscription
- ✅ Get overdue subscriptions
- ✅ Get expiring subscriptions
- ✅ Renew and cancel subscriptions
- ✅ Multi-tenant isolation

**PaymentControllerIntegrationTest**
- ✅ Initiate payment
- ✅ Verify payment (success/failure)
- ✅ Payment history
- ✅ Payment statistics
- ✅ Filter by status

### 2. Service Layer Tests

**SubscriptionServiceTest**
- ✅ Calculate next payment date
- ✅ Calculate overdue amounts with late fees
- ✅ Date calculations (monthly, yearly)
- ✅ Exception handling
- ✅ Prorated amount calculation

**SmsServiceTest**
- ✅ Format SMS messages (Persian)
- ✅ Validate phone numbers
- ✅ Calculate SMS segments
- ✅ Handle disabled SMS

### 3. Repository Tests

**SubscriptionRepositoryIntegrationTest**
- ✅ Find expiring subscriptions
- ✅ Find overdue subscriptions
- ✅ Find by customer
- ✅ Multi-tenant isolation
- ✅ Pagination support
- ✅ Count by status

### 4. Security Tests

**JwtUtilTest**
- ✅ Generate JWT tokens
- ✅ Extract claims (username, userId, businessId, role)
- ✅ Validate tokens
- ✅ Handle expired tokens
- ✅ Generate refresh tokens
- ✅ Handle tampered tokens

### 5. E2E Workflow Tests

**SubscriptionWorkflowIntegrationTest**
- ✅ Complete lifecycle: Business → Customer → Plan → Subscription → Payment
- ✅ Payment verification
- ✅ Subscription renewal workflow
- ✅ Subscription cancellation workflow

## Key Features Tested

### Authentication & Authorization
```java
// Generate test token
String token = generateTestToken("username", businessId, "BUSINESS_ADMIN");

// Use in requests
mockMvc.perform(get("/api/customers")
    .header("Authorization", "Bearer " + token))
```

### Multi-Tenant Isolation
Tests verify that:
- Business 1 cannot access Business 2's data
- Queries are automatically filtered by businessId
- Cross-tenant data leakage is prevented

### Database Integration
- Uses TestContainers PostgreSQL
- Real database queries
- Transaction rollback after each test
- Isolation between test methods

### Mock Payment Gateway
Tests simulate payment gateway callbacks:
```java
mockMvc.perform(get("/api/payments/verify")
    .param("trackingCode", trackingCode)
    .param("status", "SUCCESS"))
```

## Test Data Setup

### BaseIntegrationTest Helper Methods

```java
// Obtain real JWT token via authentication
String token = obtainJwtToken("username", "password");

// Generate test token directly (faster)
String token = generateTestToken("user", businessId, "ROLE");
```

## Assertions Examples

### Using AssertJ
```java
assertThat(result).hasSize(3);
assertThat(amount).isEqualByComparingTo(BigDecimal.valueOf(600000));
assertThat(date).isBefore(LocalDate.now());
```

### Using JsonPath (MockMvc)
```java
.andExpect(jsonPath("$.success").value(true))
.andExpect(jsonPath("$.data.firstName").value("علی"))
.andExpect(jsonPath("$.data", hasSize(3)))
```

## Common Issues & Solutions

### Issue: TestContainers timeout
**Solution**: Ensure Docker is running
```bash
docker ps
```

### Issue: Port already in use
**Solution**: TestContainers uses random ports automatically

### Issue: Database state between tests
**Solution**: Each test uses `@Transactional` or manual cleanup in `@BeforeEach`

### Issue: JWT secret too short
**Solution**: Use Base64 encoded secret (256+ bits)

## Test Coverage Goals

Current coverage targets:
- **Controller Layer**: 85%+
- **Service Layer**: 90%+
- **Repository Layer**: 80%+
- **Overall**: 70%+

Run coverage report:
```bash
mvn jacoco:report
```

View report: `target/site/jacoco/index.html`

## Continuous Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 23
        uses: actions/setup-java@v3
        with:
          java-version: '23'
      - name: Run tests
        run: mvn clean test
```

## Performance Considerations

- **TestContainers reuse**: Container reused across tests (`.withReuse(true)`)
- **Parallel execution**: Configure in `pom.xml`
- **Test data**: Minimal data creation
- **Transaction rollback**: Faster than manual cleanup

## Best Practices

1. **Isolation**: Each test is independent
2. **Clarity**: Descriptive test names with `@DisplayName`
3. **Arrange-Act-Assert**: Clear test structure
4. **Real dependencies**: Use real database, not mocks
5. **Persian support**: Test with actual Persian text
6. **Error scenarios**: Test both success and failure paths

## Debugging Tests

### Enable detailed logging
```yaml
logging:
  level:
    com.daryaftmanazam.daryaftcore: DEBUG
    org.springframework.security: DEBUG
```

### Run single test method
```bash
mvn test -Dtest=CustomerControllerIntegrationTest#testCreateCustomer_Success
```

### Print test output
```java
System.out.println("✓ Step completed: " + result);
```

## Future Enhancements

- [ ] Mutation testing with PIT
- [ ] Load testing with JMeter
- [ ] Contract testing with Pact
- [ ] BDD tests with Cucumber
- [ ] Performance benchmarking

## Contributing

When adding new tests:
1. Extend `BaseIntegrationTest` for integration tests
2. Use `@ExtendWith(MockitoExtension.class)` for unit tests
3. Add `@DisplayName` annotations
4. Follow existing naming conventions
5. Clean up test data properly
6. Update this documentation

## Support

For issues or questions:
- Check test logs in `target/surefire-reports/`
- Review TestContainers logs
- Verify Docker is running
- Check database migrations

---

**Last Updated**: December 24, 2025  
**Test Suite Version**: 1.0.0  
**Coverage**: 70%+
