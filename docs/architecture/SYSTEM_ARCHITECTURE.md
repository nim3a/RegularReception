# System Architecture

## 🏗️ معماری سیستم RegularReception

### نمای کلی (Overview)

RegularReception یک سیستم مدیریت اشتراک و پرداخت enterprise است که با معماری Layered Architecture و الگوهای Domain-Driven Design (DDD) طراحی شده است.

## 📊 Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 21
- **Build Tool**: Maven 3.9+
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15 (Production), H2 (Development)
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito, TestContainers

### Frontend
- **HTML5, CSS3, JavaScript**
- **Bootstrap 5**
- **Chart.js** (for dashboard visualizations)

### Infrastructure
- **Container**: Docker
- **Orchestration**: Docker Compose
- **Web Server**: Nginx
- **CI/CD**: GitHub Actions

## 🏛️ Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│              Presentation Layer                     │
│  (Controllers, DTOs, REST APIs, Swagger UI)         │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Service Layer                          │
│  (Business Logic, Transaction Management)           │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Repository Layer                       │
│  (Data Access, JPA Repositories)                    │
└─────────────────┬───────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────┐
│              Database Layer                         │
│  (PostgreSQL, Flyway Migrations)                    │
└─────────────────────────────────────────────────────┘
```

### Layer Details

#### 1. **Presentation Layer**
- **Controllers**: REST API endpoints
- **DTOs**: Data Transfer Objects
- **Exception Handlers**: Global exception handling
- **Security**: JWT authentication filters

**Key Components:**
- `CustomerController`
- `SubscriptionController`
- `PaymentController`
- `PaymentPlanController`
- `AuthController`

#### 2. **Service Layer**
- Business logic implementation
- Transaction management
- Data validation
- Integration with external services (SMS, Payment Gateway)

**Key Services:**
- `CustomerService`
- `SubscriptionService`
- `PaymentService`
- `SmsService`
- `NotificationService`

#### 3. **Repository Layer**
- Data access abstraction
- CRUD operations
- Custom queries

**Key Repositories:**
- `CustomerRepository`
- `SubscriptionRepository`
- `PaymentRepository`
- `PaymentPlanRepository`
- `BusinessRepository`

#### 4. **Database Layer**
- PostgreSQL for production
- H2 for development/testing
- Flyway for version control

## 🔐 Security Architecture

### Authentication Flow

```
┌──────────┐      ┌──────────────┐      ┌──────────────┐
│  Client  │─────▶│ Auth Filter  │─────▶│  JWT Utils   │
└──────────┘      └──────────────┘      └──────────────┘
                         │                       │
                         ▼                       ▼
                  ┌──────────────┐      ┌──────────────┐
                  │Security Chain│◀─────│UserDetails   │
                  └──────────────┘      │   Service    │
                         │              └──────────────┘
                         ▼
                  ┌──────────────┐
                  │ Controller   │
                  └──────────────┘
```

### Security Features
- **JWT-based authentication**
- **Role-based access control (RBAC)**
- **Password encryption (BCrypt)**
- **Multi-tenant data isolation**
- **CORS configuration**
- **Rate limiting** (planned)

## 📦 Domain Model

### Core Entities

```
┌─────────────┐
│  Business   │
└──────┬──────┘
       │ 1:N
       ▼
┌─────────────┐      1:N     ┌──────────────┐
│  Customer   │◀─────────────│Subscription  │
└─────────────┘              └──────┬───────┘
                                    │ 1:N
                                    ▼
                            ┌──────────────┐
                            │   Payment    │
                            └──────────────┘

┌─────────────┐      N:1
│Subscription │─────────────▶┌──────────────┐
└─────────────┘              │PaymentPlan   │
                             └──────────────┘
```

### Entity Relationships

1. **Business** ↔ **Customer** (One-to-Many)
   - هر کسب‌وکار چندین مشتری دارد
   - هر مشتری به یک کسب‌وکار تعلق دارد

2. **Customer** ↔ **Subscription** (One-to-Many)
   - هر مشتری می‌تواند چندین اشتراک داشته باشد
   - هر اشتراک به یک مشتری تعلق دارد

3. **Subscription** ↔ **Payment** (One-to-Many)
   - هر اشتراک چندین پرداخت دارد
   - هر پرداخت به یک اشتراک تعلق دارد

4. **Subscription** ↔ **PaymentPlan** (Many-to-One)
   - هر اشتراک یک پلن دارد
   - هر پلن می‌تواند در چندین اشتراک استفاده شود

## 🔄 Data Flow

### Subscription Creation Flow

```
Client Request
      │
      ▼
┌─────────────────────────────────────┐
│  POST /api/subscriptions            │
│  SubscriptionController             │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  SubscriptionService                │
│  - Validate customer                │
│  - Validate payment plan            │
│  - Calculate dates                  │
│  - Create subscription              │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  SubscriptionRepository             │
│  - Save to database                 │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  NotificationService                │
│  - Send SMS confirmation            │
└─────────────────────────────────────┘
```

### Payment Processing Flow

```
Client Request
      │
      ▼
┌─────────────────────────────────────┐
│  POST /api/payments                 │
│  PaymentController                  │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  PaymentService                     │
│  - Validate subscription            │
│  - Create payment record            │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  MockPaymentGateway                 │
│  - Process payment                  │
│  - Generate transaction ID          │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  PaymentService                     │
│  - Update payment status            │
│  - Update subscription              │
└─────────────────┬───────────────────┘
                  │
                  ▼
┌─────────────────────────────────────┐
│  SmsService                         │
│  - Send payment confirmation        │
└─────────────────────────────────────┘
```

## 📅 Scheduled Tasks Architecture

### Task Scheduler

```
┌─────────────────────────────────────────┐
│  Spring Task Scheduler                  │
│  (@EnableScheduling)                    │
└─────────────┬───────────────────────────┘
              │
    ┌─────────┴─────────┬─────────────────┐
    │                   │                 │
    ▼                   ▼                 ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│  Overdue    │  │  Expire     │  │  Payment    │
│  Check      │  │  Check      │  │  Reminder   │
│  (2 AM)     │  │  (3 AM)     │  │  (9 AM)     │
└─────────────┘  └─────────────┘  └─────────────┘
```

### Scheduled Tasks

1. **Overdue Check** (Daily at 2 AM)
   - بررسی اشتراک‌های عقب‌افتاده
   - تغییر وضعیت به OVERDUE

2. **Expire Check** (Daily at 3 AM)
   - بررسی اشتراک‌های منقضی‌شده
   - تغییر وضعیت به EXPIRED

3. **Payment Reminder** (Daily at 9 AM)
   - ارسال یادآوری پرداخت
   - 3 روز قبل از سررسید

4. **Pending SMS Processing** (Every 5 minutes)
   - پردازش SMS‌های در صف
   - retry برای failed messages

## 🔌 External Integrations

### SMS Service (Melipayamak)

```
┌─────────────────┐
│  SmsService     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐      HTTP       ┌──────────────────┐
│  RestTemplate   │─────────────────▶│  Melipayamak    │
└─────────────────┘                  │  API             │
                                     └──────────────────┘
```

### Payment Gateway (Mock)

```
┌─────────────────┐
│PaymentService   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│MockPaymentGateway│
│  - 80% Success  │
│  - 20% Failure  │
└─────────────────┘
```

## 🧪 Testing Architecture

### Test Pyramid

```
        ┌───────────┐
        │    E2E    │  WorkflowTests (پرداخت کامل)
        │  Tests    │
        └───────────┘
       /             \
      /               \
  ┌────────────────────┐
  │ Integration Tests  │  Controller + Repository Tests
  │  (TestContainers)  │
  └────────────────────┘
   /                   \
  /                     \
┌──────────────────────────┐
│     Unit Tests           │  Service Layer Tests
│   (Mocks + Stubs)        │
└──────────────────────────┘
```

### Test Types

1. **Unit Tests**
   - Service layer testing با Mockito
   - Business logic validation
   - Fast execution

2. **Integration Tests**
   - Controller tests با MockMvc
   - Repository tests با TestContainers
   - Real database interactions

3. **E2E Tests**
   - Complete workflow testing
   - از ایجاد مشتری تا پرداخت

## 🐳 Deployment Architecture

### Docker Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Docker Host                       │
│                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │    Nginx     │  │   Backend    │  │PostgreSQL│ │
│  │   (Port 80)  │  │  (Port 8081) │  │(Port 5432│ │
│  └──────┬───────┘  └──────┬───────┘  └────┬─────┘ │
│         │                 │                │       │
│         └─────────────────┴────────────────┘       │
│                 app-network                        │
└─────────────────────────────────────────────────────┘
```

### Container Communication

- **Nginx** → **Backend**: Reverse proxy
- **Backend** → **PostgreSQL**: Database connection
- **All containers**: Connected via `app-network` bridge

## 📈 Performance Considerations

### Database Optimization

1. **Indexes**:
   - `idx_customer_phone` on `phone_number`
   - `idx_subscription_status` on `status`
   - `idx_subscription_end_date` on `end_date`
   - `idx_payment_status` on `status`

2. **Connection Pooling**:
   - HikariCP (default in Spring Boot)
   - `maximum-pool-size: 10`

3. **Query Optimization**:
   - Lazy loading for collections
   - Pagination for large result sets
   - Efficient JPA queries

### Caching Strategy (Planned)

- Spring Cache with Redis
- Cache subscription plans
- Cache business configurations

## 🔍 Monitoring & Observability

### Logging

- **Framework**: SLF4J + Logback
- **Levels**: DEBUG (dev), INFO (prod)
- **Format**: Timestamp + Message

### Health Checks

- Spring Boot Actuator
- `/actuator/health` endpoint
- Database connectivity check

### Metrics (Planned)

- Micrometer + Prometheus
- JVM metrics
- HTTP request metrics
- Database query metrics

## 🚀 Scalability

### Horizontal Scaling

```
┌────────────┐
│   Nginx    │  Load Balancer
└─────┬──────┘
      │
   ┌──┴───┬──────────┐
   │      │          │
   ▼      ▼          ▼
┌────┐ ┌────┐    ┌────┐
│App1│ │App2│ ...│AppN│  Multiple instances
└──┬─┘ └──┬─┘    └──┬─┘
   │      │          │
   └──────┴──────────┘
          │
          ▼
   ┌────────────┐
   │ PostgreSQL │  Shared database
   └────────────┘
```

### Future Improvements

1. **Database**:
   - Read replicas
   - Connection pooling
   - Query optimization

2. **Caching**:
   - Redis for session storage
   - Cache frequently accessed data

3. **Message Queue**:
   - RabbitMQ for async processing
   - SMS queue management

4. **Microservices** (Long-term):
   - Separate services for:
     - Customer Management
     - Payment Processing
     - Notification Service

## 📝 Design Patterns Used

1. **Layered Architecture**: Separation of concerns
2. **Repository Pattern**: Data access abstraction
3. **DTO Pattern**: Data transfer between layers
4. **Service Pattern**: Business logic encapsulation
5. **Builder Pattern**: Complex object construction
6. **Strategy Pattern**: Payment gateway implementations
7. **Observer Pattern**: Event-driven notifications
8. **Factory Pattern**: Entity creation

## 📚 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Performance_Optimization)
