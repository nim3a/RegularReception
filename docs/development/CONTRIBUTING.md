# Contributing to RegularReception

## 🤝 مشارکت در توسعه

از اینکه می‌خواهید در توسعه RegularReception مشارکت کنید، متشکریم! این راهنما به شما کمک می‌کند تا به بهترین شکل ممکن مشارکت کنید.

## 📋 جدول محتویات

- [Code of Conduct](#code-of-conduct)
- [چگونه می‌توانم مشارکت کنم؟](#how-can-i-contribute)
- [راه‌اندازی محیط توسعه](#development-setup)
- [راهنمای کدنویسی](#coding-guidelines)
- [فرآیند Pull Request](#pull-request-process)
- [پیام‌های Commit](#commit-messages)
- [گزارش مشکلات](#reporting-bugs)
- [پیشنهاد ویژگی‌های جدید](#suggesting-features)

## Code of Conduct

این پروژه و همه مشارکت‌کنندگان آن باید از [Code of Conduct](CODE_OF_CONDUCT.md) پیروی کنند. با مشارکت در این پروژه، انتظار می‌رود که این کد را رعایت کنید.

## چگونه می‌توانم مشارکت کنم؟

### 1. گزارش باگ‌ها

- قبل از گزارش، [Issues موجود](https://github.com/nim3a/RegularReception/issues) را بررسی کنید
- از template گزارش باگ استفاده کنید
- اطلاعات کامل و دقیق ارائه دهید
- مراحل بازتولید مشکل را شرح دهید
- اسکرین‌شات یا log اضافه کنید

### 2. پیشنهاد ویژگی جدید

- ابتدا یک Issue باز کنید تا در مورد تغییرات بزرگ بحث شود
- توضیح دهید که چرا این ویژگی مفید است
- مثال‌های کاربردی ارائه دهید

### 3. پیاده‌سازی کد

- Fork کردن repository
- ایجاد branch جدید
- پیاده‌سازی تغییرات
- نوشتن تست
- ارسال Pull Request

## راه‌اندازی محیط توسعه

### پیش‌نیازها

```bash
# Java 21
java -version

# Maven 3.9+
mvn -version

# Docker & Docker Compose
docker --version
docker-compose --version

# Git
git --version
```

### کلون و راه‌اندازی

```bash
# 1. Fork repository در GitHub
# 2. کلون کردن fork خود
git clone https://github.com/YOUR_USERNAME/RegularReception.git
cd RegularReception

# 3. اضافه کردن upstream
git remote add upstream https://github.com/nim3a/RegularReception.git

# 4. راه‌اندازی دیتابیس
cd docker
docker-compose up -d postgres

# 5. اجرای برنامه
mvn spring-boot:run

# 6. اجرای تست‌ها
mvn test
```

### ساختار پروژه

```
src/
├── main/
│   ├── java/com/daryaftmanazam/daryaftcore/
│   │   ├── controller/    # REST Controllers
│   │   ├── service/       # Business Logic
│   │   ├── repository/    # Data Access
│   │   ├── model/         # Entities
│   │   ├── dto/           # DTOs
│   │   ├── config/        # Configurations
│   │   ├── security/      # Security
│   │   ├── exception/     # Exception Handling
│   │   └── util/          # Utilities
│   └── resources/
│       ├── application.yml
│       ├── db/migration/  # Flyway migrations
│       └── messages_fa_IR.properties
└── test/
    └── java/              # Test files
```

## راهنمای کدنویسی

### استایل کد Java

#### 1. قراردادهای نام‌گذاری

```java
// Classes: PascalCase
public class CustomerService { }

// Methods: camelCase
public Customer findCustomerById(Long id) { }

// Constants: UPPER_SNAKE_CASE
public static final String DEFAULT_CURRENCY = "IRR";

// Variables: camelCase
private String customerName;
```

#### 2. JavaDoc

همیشه برای کلاس‌ها و متدهای public JavaDoc بنویسید:

```java
/**
 * سرویس مدیریت مشتریان
 * 
 * این سرویس عملیات CRUD برای مشتریان را مدیریت می‌کند.
 * 
 * @author Nim3a
 * @version 1.0.0
 * @since 2025-01-01
 */
@Service
public class CustomerService {
    
    /**
     * جستجوی مشتری با شناسه
     * 
     * @param id شناسه مشتری
     * @return مشتری یافت شده
     * @throws CustomerNotFoundException اگر مشتری یافت نشود
     */
    public Customer findById(Long id) {
        // implementation
    }
}
```

#### 3. مدیریت Exception

```java
// استفاده از custom exceptions
throw new CustomerNotFoundException("Customer not found: " + id);

// نه generic exceptions
// BAD: throw new Exception("Error");
```

#### 4. Validation

```java
@Service
public class CustomerService {
    
    public Customer createCustomer(@Valid CustomerDTO dto) {
        // Validation از طریق @Valid
        // Business validation
        if (existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException();
        }
        // ...
    }
}
```

#### 5. Transaction Management

```java
@Service
@Transactional
public class PaymentService {
    
    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        // read-only transaction
    }
    
    @Transactional
    public Payment processPayment(Long id) {
        // write transaction
    }
}
```

### تست‌نویسی

#### 1. Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository repository;
    
    @InjectMocks
    private CustomerService service;
    
    @Test
    @DisplayName("باید مشتری را با ID پیدا کند")
    void shouldFindCustomerById() {
        // Given
        Long customerId = 1L;
        Customer customer = new Customer();
        when(repository.findById(customerId))
            .thenReturn(Optional.of(customer));
        
        // When
        Customer result = service.findById(customerId);
        
        // Then
        assertNotNull(result);
        verify(repository).findById(customerId);
    }
}
```

#### 2. Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateCustomer() throws Exception {
        // Given
        String customerJson = """
            {
                "name": "علی احمدی",
                "phoneNumber": "09123456789"
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("علی احمدی"));
    }
}
```

### Database Migrations

#### قوانین Flyway

1. **نام‌گذاری فایل‌ها:**
   ```
   V{version}__{description}.sql
   مثال: V8__add_customer_email_index.sql
   ```

2. **محتوای Migration:**
   ```sql
   -- V8__add_customer_email_index.sql
   
   -- Add index on customer email for faster lookups
   CREATE INDEX idx_customer_email ON customers(email);
   
   -- Add comment
   COMMENT ON INDEX idx_customer_email IS 'Index for email lookups';
   ```

3. **قوانین:**
   - هیچ‌گاه migration موجود را تغییر ندهید
   - همیشه migration جدید اضافه کنید
   - از transaction استفاده کنید
   - Rollback plan داشته باشید

## فرآیند Pull Request

### 1. ایجاد Branch

```bash
# همیشه از main جدیدترین نسخه را بگیرید
git checkout main
git pull upstream main

# branch جدید بسازید
git checkout -b feature/add-customer-export

# یا برای bugfix:
git checkout -b fix/customer-validation-bug
```

### 2. نام‌گذاری Branch

قالب: `type/description`

**Types:**
- `feature/` - ویژگی جدید
- `fix/` - رفع باگ
- `refactor/` - بازنویسی کد
- `test/` - اضافه کردن تست
- `docs/` - مستندات
- `chore/` - کارهای maintenance

**مثال‌ها:**
```
feature/add-payment-export
fix/subscription-date-calculation
refactor/improve-sms-service
test/add-payment-tests
docs/update-api-documentation
```

### 3. کد بنویسید

```bash
# تغییرات خود را اعمال کنید
# تست‌ها را اجرا کنید
mvn test

# Code style را بررسی کنید
mvn checkstyle:check

# Coverage را بررسی کنید
mvn clean verify jacoco:report
```

### 4. Commit کنید

```bash
# تغییرات را stage کنید
git add .

# commit با پیام مناسب
git commit -m "feat(customer): add customer export functionality"
```

### 5. Push و PR

```bash
# push به fork خود
git push origin feature/add-customer-export

# در GitHub یک Pull Request باز کنید
```

### 6. PR Template

```markdown
## Description
توضیح مختصری از تغییرات

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## How Has This Been Tested?
- [ ] Unit tests
- [ ] Integration tests
- [ ] Manual testing

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests added
- [ ] All tests pass
```

## پیام‌های Commit

### قالب

```
type(scope): subject

body (optional)

footer (optional)
```

### Types

- `feat`: ویژگی جدید
- `fix`: رفع باگ
- `docs`: تغییرات مستندات
- `style`: فرمت، semicolon، whitespace و غیره
- `refactor`: بازنویسی کد بدون تغییر عملکرد
- `test`: اضافه/اصلاح تست‌ها
- `chore`: تغییرات build، CI و غیره
- `perf`: بهبود performance

### Scope

- `customer`: مربوط به Customer
- `subscription`: مربوط به Subscription
- `payment`: مربوط به Payment
- `sms`: مربوط به SMS
- `auth`: مربوط به Authentication
- `api`: مربوط به API
- `db`: مربوط به Database
- `config`: مربوط به Configuration

### مثال‌ها

```bash
# Feature
git commit -m "feat(customer): add customer export to Excel functionality"

# Bug fix
git commit -m "fix(subscription): correct end date calculation for leap years"

# Documentation
git commit -m "docs(api): update REST API documentation"

# Refactor
git commit -m "refactor(sms): improve error handling in SMS service"

# با body
git commit -m "feat(payment): add payment reminder scheduler

- Add scheduled task to send payment reminders
- Send SMS 3 days before payment due date
- Add configuration for reminder timing

Closes #123"
```

## گزارش مشکلات

### Bug Report Template

```markdown
**توضیح باگ**
توضیح واضح و مختصر از باگ

**مراحل بازتولید**
1. برو به '...'
2. کلیک کن روی '...'
3. Scroll به '...'
4. خطا را ببین

**رفتار مورد انتظار**
توضیح دهید که چه اتفاقی باید می‌افتاد

**Screenshots**
در صورت امکان اسکرین‌شات اضافه کنید

**محیط:**
 - OS: [e.g. Windows 11]
 - Java Version: [e.g. 21.0.1]
 - Browser: [e.g. Chrome 120]
 - Version: [e.g. 1.0.0]

**Logs**
```
log output here
```

**اطلاعات اضافی**
هر اطلاعات دیگری که مفید است
```

## پیشنهاد ویژگی‌های جدید

### Feature Request Template

```markdown
**آیا feature request شما مربوط به یک مشکل است؟**
توضیح واضح از مشکل. مثال: همیشه ناراحت‌کننده است وقتی که [...]

**راه‌حل پیشنهادی**
توضیح واضح از آنچه می‌خواهید اتفاق بیفتد

**جایگزین‌های در نظر گرفته شده**
توضیح راه‌حل‌های جایگزینی که در نظر گرفته‌اید

**زمینه اضافی**
اضافه کردن هر زمینه یا اسکرین‌شات دیگری
```

## Review Process

### کد شما توسط maintainerها بررسی می‌شود:

1. **Code Review:**
   - Style guidelines
   - Best practices
   - Performance
   - Security

2. **Test Review:**
   - Test coverage
   - Test quality
   - Edge cases

3. **Documentation Review:**
   - JavaDoc
   - README updates
   - API documentation

### Checklist برای Reviewers:

- [ ] کد از coding guidelines پیروی می‌کند
- [ ] تست‌ها نوشته شده و pass می‌کنند
- [ ] Coverage کافی است (>80%)
- [ ] مستندات به‌روز است
- [ ] هیچ regression bug وجود ندارد
- [ ] Performance قابل قبول است
- [ ] Security issues وجود ندارد

## منابع

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [TestContainers](https://www.testcontainers.org/)
- [Conventional Commits](https://www.conventionalcommits.org/)

## سوالات؟

اگر سوالی دارید:

- Issue باز کنید
- در [Discussions](https://github.com/nim3a/RegularReception/discussions) سوال بپرسید
- Email: nim3a@example.com

---

**متشکریم که به RegularReception کمک می‌کنید! 🎉**
