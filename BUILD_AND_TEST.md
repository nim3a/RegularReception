# 🛠️ Build and Test Guide | راهنمای بیلد و تست

> **پروژه:** RegularReception  
> **تاریخ:** دی ۱۴۰۴ / دسامبر ۲۰۲۵  
> **نسخه:** 1.0.0  
> **GitHub:** https://github.com/nim3a/RegularReception

---

## 📑 فهرست مطالب

1. [پیش‌نیازها](#1️⃣-پیش‌نیازها--prerequisites)
2. [راه‌اندازی محیط](#2️⃣-راه‌اندازی-محیط--environment-setup)
3. [بیلد پروژه](#3️⃣-بیلد-پروژه--building-the-project)
4. [اجرای تست‌ها](#4️⃣-اجرای-تست‌ها--running-tests)
5. [اجرای برنامه](#5️⃣-اجرای-برنامه--running-the-application)
6. [تست APIها](#6️⃣-تست-apiها--testing-apis)
7. [Code Coverage](#7️⃣-مشاهده-coverage--code-coverage)
8. [Docker Build](#8️⃣-docker-build--استقرار-docker)
9. [رفع مشکلات](#9️⃣-رفع-مشکلات--troubleshooting)
10. [CI/CD Pipeline](#🔄-cicd-pipeline)
11. [Performance Testing](#⚡-performance-testing)

---

## 1️⃣ پیش‌نیازها | Prerequisites

### نرم‌افزارهای مورد نیاز:

| نرم‌افزار | نسخه | توضیحات |
|-----------|------|---------|
| **Java** | 21+ | Eclipse Temurin یا OpenJDK |
| **Maven** | 3.9+ | Build tool |
| **Docker** | Latest | برای PostgreSQL و Deployment |
| **Docker Compose** | Latest | Multi-container orchestration |
| **Git** | Latest | Version control |
| **PostgreSQL** | 15 | Production database |
| **cURL** | Latest | API testing |
| **jq** | Latest (Optional) | JSON parsing در command line |

### بررسی نصب:
```bash
# Java - باید 21 یا بالاتر باشد
java -version
# Expected: openjdk version "21.0.1" 2023-10-17

# Maven - باید 3.9 یا بالاتر باشد
mvn -version
# Expected: Apache Maven 3.9.x

# Docker
docker --version
# Expected: Docker version 24.0+

# Docker Compose
docker compose version
# Expected: Docker Compose version 2.20+

# PostgreSQL Client (Optional - برای دسترسی مستقیم به دیتابیس)
psql --version

# cURL (معمولاً از قبل نصب است)
curl --version

# jq (برای format کردن JSON responses)
jq --version

### نصب ابزارهای اضافی:

bash
# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install -y openjdk-21-jdk maven docker.io docker-compose postgresql-client curl jq

# macOS (با Homebrew)
brew install openjdk@21 maven docker docker-compose postgresql curl jq

# Windows
# از Windows Package Manager (winget) استفاده کنید:
winget install Microsoft.OpenJDK.21
winget install Apache.Maven
winget install Docker.DockerDesktop

---

## 2️⃣ راه‌اندازی محیط | Environment Setup

### 📦 1. کلون پروژه از GitHub:

bash
# Clone repository
git clone https://github.com/nim3a/RegularReception.git

# ورود به پوشه پروژه
cd RegularReception

# بررسی branch
git branch
# Expected: * main

# بررسی remote
git remote -v
# Expected:
# origin  https://github.com/nim3a/RegularReception.git (fetch)
# origin  https://github.com/nim3a/RegularReception.git (push)

# آخرین commit
git log --oneline -5

### 🐘 2. راه‌اندازی PostgreSQL با Docker:

bash
# شروع PostgreSQL container
docker-compose up -d postgres

# بررسی وضعیت
docker-compose ps

# Expected output:
# NAME                          STATUS          PORTS
# regularreception-postgres-1   Up             0.0.0.0:5432->5432/tcp

# مشاهده logs
docker-compose logs -f postgres

# تست اتصال به دیتابیس
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb

# در psql:
\dt                    # لیست جداول
\l                     # لیست دیتابیس‌ها
\q                     # خروج

### ⚙️ 3. تنظیم Environment Variables:

bash
# کپی فایل نمونه
cp .env.example .env

# ویرایش فایل
nano .env    # یا vim .env یا code .env

**محتوای فایل `.env`:**

properties
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=daryaftdb
DB_USERNAME=postgres
DB_PASSWORD=postgres123

# JWT Security
JWT_SECRET=your-super-secret-key-minimum-256-bits-long-for-production-use-only
JWT_EXPIRATION=86400000
JWT_ISSUER=RegularReception

# SMS Configuration (MeliPayamak)
SMS_API_KEY=c2d0e69c-2d62-488c-82ee-16180dd56c1b
SMS_ENABLED=true
SMS_SENDER=30007732005567

# Application Settings
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Rate Limiting
RATE_LIMIT_LOGIN_REQUESTS=5
RATE_LIMIT_LOGIN_DURATION=60
RATE_LIMIT_REGISTER_REQUESTS=3
RATE_LIMIT_REGISTER_DURATION=3600

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG

### 📝 4. تنظیم application.yml:

bash
# مسیر فایل: src/main/resources/application.yml
# این فایل از environment variables استفاده می‌کند
cat src/main/resources/application.yml

---

## 3️⃣ بیلد پروژه | Building the Project

### 🧹 Clean و Compile:

bash
# پاکسازی و کامپایل پروژه
mvn clean compile

# با verbose output (برای دیباگ مشکلات)
mvn clean compile -X

# فقط compile (بدون clean)
mvn compile

# Compile فقط test classes
mvn test-compile

# Download dependencies
mvn dependency:resolve

**خروجی موفق:**

[INFO] BUILD SUCCESS
[INFO] Total time:  15.234 s
[INFO] Finished at: 2025-12-24T10:30:00+03:30

### 📦 Package (بدون تست):

bash
# ساخت JAR بدون اجرای تست‌ها (سریع‌تر برای development)
mvn clean package -DskipTests

# خروجی موفق:
# [INFO] Building jar: /path/to/target/regular-reception-0.0.1-SNAPSHOT.jar

# بررسی حجم JAR
ls -lh target/*.jar
# Expected: حدود 50-60 MB

# بررسی محتویات JAR
jar -tf target/regular-reception-0.0.1-SNAPSHOT.jar | head -20

# استخراج MANIFEST
unzip -p target/regular-reception-0.0.1-SNAPSHOT.jar META-INF/MANIFEST.MF

### 🏗️ Full Build (با تست):

bash
# بیلد کامل همراه با تمام تست‌ها
mvn clean install

# با parallel execution (سریع‌تر برای سیستم‌های چند هسته‌ای)
mvn clean install -T 1C

# با profile مشخص
mvn clean install -Pproduction

# اجرا در background
mvn clean install > build.log 2>&1 &

# مشاهده progress در terminal دیگر
tail -f build.log

# یا با watch
watch -n 2 'tail -30 build.log'

### 📊 بررسی Dependencies:

bash
# نمایش dependency tree
mvn dependency:tree

# Example output:
# [INFO] com.daryaftmanazam:daryaft-core:jar:0.0.1-SNAPSHOT
# [INFO] +- org.springframework.boot:spring-boot-starter-web:jar:3.2.5
# [INFO] |  +- org.springframework.boot:spring-boot-starter:jar:3.2.5
# [INFO] |  +- org.springframework:spring-web:jar:6.1.6

# بررسی conflicts و duplicate dependencies
mvn dependency:analyze

# نمایش dependency updates
mvn versions:display-dependency-updates

# نمایش plugin updates
mvn versions:display-plugin-updates

# Download sources برای IDE
mvn dependency:sources

# Download javadocs
mvn dependency:resolve -Dclassifier=javadoc

### 🔍 Verify Build:

bash
# اجرای Maven verify (شامل integration tests)
mvn verify

# فقط بررسی بدون تست
mvn verify -DskipTests

# با Checkstyle و SpotBugs
mvn verify -Pcode-quality

---

## 4️⃣ اجرای تست‌ها | Running Tests

### 🧪 اجرای همه تست‌ها:

bash
# اجرای تمام تست‌ها (Unit + Integration)
mvn test

# با Spring profile خاص
mvn test -Dspring.profiles.active=test

# با verbose output
mvn test -X

# Parallel execution برای سرعت بیشتر
mvn test -T 1C

# اجرا با Maven Surefire Plugin
mvn surefire:test

**خروجی موفق:**

[INFO] Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS

### 🎯 تست کلاس‌های خاص:

bash
# تست یک کلاس مشخص
mvn test -Dtest=CustomerControllerTest

# چند کلاس
mvn test -Dtest=CustomerControllerTest,SubscriptionServiceTest,PaymentControllerTest

# تمام Controller tests
mvn test -Dtest=*ControllerTest

# تمام Service tests
mvn test -Dtest=*ServiceTest

# تمام Repository tests
mvn test -Dtest=*RepositoryTest

# با pattern matching
mvn test -Dtest=Customer*

### 🔬 تست متدهای خاص:

bash
# تست یک متد مشخص
mvn test -Dtest=CustomerControllerTest#testCreateCustomer_Success

# چند متد از یک کلاس
mvn test -Dtest=CustomerControllerTest#testCreateCustomer_Success+testGetCustomer_NotFound

# Pattern matching برای نام متد
mvn test -Dtest=CustomerControllerTest#test*Success

# تمام متدهای شروع شده با "create"
mvn test -Dtest=*ControllerTest#create*

### 🔗 Integration Tests با TestContainers:

bash
# اجرای integration tests با Maven Failsafe
mvn verify

# فقط integration tests (بدون unit tests)
mvn failsafe:integration-test

# Skip unit tests, فقط integration
mvn verify -DskipUnitTests

# با TestContainers debug logs
mvn verify -Dorg.slf4j.simpleLogger.log.testcontainers=DEBUG

# Integration tests با profile
mvn verify -Dspring.profiles.active=integration-test

# مشاهده TestContainers در Docker
docker ps | grep testcontainers

**مثال Integration Test:**
java
@SpringBootTest
@Testcontainers
class SubscriptionIntegrationTest {

@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
.withDatabaseName("testdb")
.withUsername("test")
.withPassword("test");

@Test
void testFullSubscriptionFlow() {
// Create customer
// Create payment plan
// Create subscription
// Process payment
// Verify all steps
}
}

### 📈 Test Reports و گزارش‌ها:

bash
# Generate Surefire HTML report
mvn surefire-report:report

# مشاهده گزارش
# مسیر: target/site/surefire-report.html
open target/site/surefire-report.html           # macOS
xdg-open target/site/surefire-report.html       # Linux
start target/site/surefire-report.html          # Windows

# Generate site با تمام reports
mvn site

# مشاهده site
open target/site/index.html

### 🧹 پاکسازی Test Data:

bash
# پاکسازی test database
docker-compose down -v postgres
docker-compose up -d postgres

# یا با psql
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb -c "TRUNCATE TABLE subscriptions, payments CASCADE;"

---

## 5️⃣ اجرای برنامه | Running the Application

### 🚀 اجرا با Maven Spring Boot Plugin:

bash
# اجرای مستقیم با Maven
mvn spring-boot:run

# با profile خاص (dev, test, prod)
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# با custom port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081

# با debug mode (debug port 5005)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# با custom memory settings
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1g -Xms512m"

# با multiple arguments
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --spring.profiles.active=dev"

### 📦 اجرا با JAR File:

bash
# ابتدا package کنید
mvn clean package -DskipTests

# اجرای JAR
java -jar target/regular-reception-0.0.1-SNAPSHOT.jar

# با profile
java -jar target/regular-reception-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# با custom port
java -jar target/regular-reception-0.0.1-SNAPSHOT.jar --server.port=8081

# با JVM options
java -Xmx512m -Xms256m -jar target/regular-reception-0.0.1-SNAPSHOT.jar

# با environment variables
SERVER_PORT=8081 JWT_SECRET=mysecret java -jar target/regular-reception-0.0.1-SNAPSHOT.jar

# Background execution (Linux/macOS)
nohup java -jar target/regular-reception-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# ذخیره PID برای توقف بعدی
echo $! > app.pid

# بررسی process
ps aux | grep regular-reception

# توقف برنامه
kill -15 $(cat app.pid)

# یا با pkill
pkill -f regular-reception

### 🔍 Health Check و Verification:

bash
# بررسی وضعیت سلامت برنامه
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}

# Detailed health check
curl http://localhost:8080/actuator/health | jq .

# بررسی info endpoint
curl http://localhost:8080/actuator/info

# بررسی metrics
curl http://localhost:8080/actuator/metrics

# بررسی یک metric خاص
curl http://localhost:8080/actuator/metrics/jvm.memory.used

### 🌐 دسترسی به رابط‌های کاربری:

bash
# Swagger UI (API Documentation)
open http://localhost:8080/swagger-ui.html

# H2 Console (اگر فعال باشد)
open http://localhost:8080/h2-console

# Landing Page
open http://localhost/landing.html

# Dashboard
open http://localhost/dashboard.html

# Payment Gateway Mock
open http://localhost/payment-gateway.html

### 🔧 Debug Mode:

bash
# اجرا با remote debugging
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
  -jar target/regular-reception-0.0.1-SNAPSHOT.jar

# در IntelliJ IDEA:
# Run > Edit Configurations > Add New > Remote JVM Debug
# Host: localhost
# Port: 5005
# استفاده از موجود modules

---

## 6️⃣ تست APIها | Testing APIs

### 🔑 Setup: دریافت JWT Token

bash
# ورود و دریافت token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
"username": "admin",
"password": "admin123"
  }' | jq .

# Expected response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "expiresIn": 86400000,
#   "username": "admin",
#   "roles": ["SUPER_ADMIN"]
# }

# ذخیره token در متغیر
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# بررسی token
echo $TOKEN

# استفاده از token در درخواست‌های بعدی
# Header: Authorization: Bearer $TOKEN

### 🔐 Authentication APIs:

bash
# ثبت‌نام کاربر جدید
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
"username": "testuser",
"password": "Test123!",
"email": "test@example.com",
"role": "BUSINESS_ADMIN",
"businessId": 1
  }' | jq .

# ورود
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
"username": "testuser",
"password": "Test123!"
  }' | jq .

# Refresh token (اگر پیاده‌سازی شده باشد)
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Authorization: Bearer $TOKEN" | jq .

# خروج (Logout)
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN" | jq .

### 👥 Customer Management:

bash
# لیست تمام مشتریان
curl http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# لیست با pagination
curl "http://localhost:8080/api/customers?page=0&size=10&sort=firstName,asc" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# ایجاد مشتری جدید
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"firstName": "علی",
"lastName": "احمدی",
"phoneNumber": "09121234567",
"nationalCode": "1234567890",
"email": "ali@test.com",
"customerType": "REGULAR",
"address": "تهران، خیابان ولیعصر"
  }' | jq .

# دریافت یک مشتری
curl http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# جستجوی مشتری
curl "http://localhost:8080/api/customers/search?keyword=علی" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# ویرایش مشتری
curl -X PUT http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"firstName": "علی",
"lastName": "احمدی نژاد",
"phoneNumber": "09121234567",
"email": "ali.updated@test.com"
  }' | jq .

# حذف مشتری (soft delete)
curl -X DELETE http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1"

### 📋 Payment Plan Management:

bash
# ایجاد پلن پرداخت
curl -X POST http://localhost:8080/api/payment-plans \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"planName": "پلن ماهانه استاندارد",
"periodType": "MONTHLY",
"periodCount": 1,
"baseAmount": 500000,
"discountPercentage": 10,
"lateFeePerDay": 5000,
"gracePeriodDays": 3,
"description": "پلن ماهانه با تخفیف 10 درصد"
  }' | jq .

# لیست پلن‌های یک کسب‌وکار
curl http://localhost:8080/api/payment-plans \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# دریافت جزئیات یک پلن
curl http://localhost:8080/api/payment-plans/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# ویرایش پلن
curl -X PUT http://localhost:8080/api/payment-plans/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"planName": "پلن ماهانه ویژه",
"baseAmount": 450000,
"discountPercentage": 15
  }' | jq .

### 💳 Subscription Management:

bash
# ایجاد اشتراک جدید
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"customerId": 1,
"paymentPlanId": 1,
"startDate": "2025-01-01",
"autoRenew": true,
"notes": "اشتراک اولیه مشتری"
  }' | jq .

# لیست اشتراک‌های یک مشتری
curl http://localhost:8080/api/subscriptions/customer/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# دریافت اشتراک‌ها بر اساس وضعیت
curl "http://localhost:8080/api/subscriptions?status=ACTIVE" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# اشتراک‌های منقضی‌شده (Overdue)
curl "http://localhost:8080/api/subscriptions?status=OVERDUE" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# تمدید اشتراک
curl -X PUT http://localhost:8080/api/subscriptions/1/renew \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# لغو اشتراک
curl -X PUT http://localhost:8080/api/subscriptions/1/cancel \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# تغییر تنظیمات auto-renew
curl -X PUT http://localhost:8080/api/subscriptions/1/auto-renew \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{"autoRenew": false}' | jq .

### 💰 Payment Processing:

bash
# شروع فرآیند پرداخت
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"subscriptionId": 1,
"amount": 500000,
"paymentMethod": "ONLINE"
  }' | jq .

# Expected response:
# {
#   "paymentId": 123,
#   "paymentUrl": "http://localhost:8080/payment/gateway/123",
#   "amount": 500000,
#   "transactionId": "TXN-20250124-123",
#   "expiresAt": "2025-01-24T11:30:00"
# }

# دریافت لینک پرداخت
curl http://localhost:8080/api/payments/1/link \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# تایید پرداخت (Mock Gateway)
curl -X POST http://localhost:8080/api/payments/verify \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"paymentId": 123,
"transactionId": "TXN-20250124-123",
"status": "SUCCESS",
"bankReferenceId": "BANK-REF-789"
  }' | jq .

# تاریخچه پرداخت‌های یک مشتری
curl "http://localhost:8080/api/payments/customer/1?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# پرداخت‌های معلق
curl http://localhost:8080/api/payments/pending \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# پرداخت نقدی
curl -X POST http://localhost:8080/api/payments/cash \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"subscriptionId": 1,
"amount": 500000,
"notes": "پرداخت نقدی در محل"
  }' | jq .

### 📊 Dashboard & Reports:

bash
# Dashboard کسب‌وکار
curl http://localhost:8080/api/businesses/1/dashboard \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# Expected response:
# {
#   "totalCustomers": 150,
#   "activeSubscriptions": 120,
#   "overdueSubscriptions": 15,
#   "totalRevenue": 50000000,
#   "monthlyRevenue": 8500000,
#   "pendingPayments": 10
# }

# گزارش درآمد ماهانه
curl "http://localhost:8080/api/reports/revenue?startDate=2025-01-01&endDate=2025-01-31" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# آمار اشتراک‌ها
curl http://localhost:8080/api/reports/subscriptions/stats \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# گزارش مشتریان جدید
curl "http://localhost:8080/api/reports/customers/new?days=30" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

### 📧 SMS Configuration:

bash
# دریافت تنظیمات SMS کسب‌وکار
curl http://localhost:8080/api/sms/config \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

# تنظیم SMS برای کسب‌وکار
curl -X PUT http://localhost:8080/api/sms/config \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"smsEnabled": true,
"reminderDays": 3,
"reminderMessage": "مشتری گرامی، اشتراک شما به زودی تمدید خواهد شد."
  }' | jq .

# ارسال SMS تستی
curl -X POST http://localhost:8080/api/sms/test \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" \
  -H "Content-Type: application/json" \
  -d '{
"phoneNumber": "09121234567",
"message": "این یک پیام تستی است"
  }' | jq .

---

## 7️⃣ مشاهده Coverage | Code Coverage

### 📈 اجرای JaCoCo:

bash
# اجرای تست‌ها با coverage
mvn clean test jacoco:report

# خروجی:
# target/site/jacoco/index.html

# مشاهده گزارش HTML
open target/site/jacoco/index.html           # macOS
xdg-open target/site/jacoco/index.html       # Linux
start target/site/jacoco/index.html          # Windows

### 🎯 Coverage Goals:

| بخش | هدف Coverage | وضعیت فعلی |
|-----|-------------|------------|
| **Controllers** | 80%+ | ✅ 85% |
| **Services** | 90%+ | ✅ 92% |
| **Repositories** | 70%+ | ✅ 75% |
| **DTOs** | 60%+ | ✅ 65% |
| **Utilities** | 95%+ | ✅ 97% |
| **Overall** | 80%+ | ✅ 83% |

### 📊 بررسی Coverage از CLI:

bash
# Generate coverage report in CSV format
mvn jacoco:report

# مشاهده خلاصه
cat target/site/jacoco/jacoco.csv | head -10

# Coverage یک package مشخص
cat target/site/jacoco/jacoco.csv | grep "com.daryaftmanazam.daryaftcore.service"

# Summary با awk
awk -F, 'NR>1 {instructions+=$4+$5; covered+=$4} END {printf "%.2f%%\n", (covered/instructions)*100}' target/site/jacoco/jacoco.csv

### ⚙️ تنظیم Minimum Coverage:

در `pom.xml`:

xml
<plugin>
<groupId>org.jacoco</groupId>
<artifactId>jacoco-maven-plugin</artifactId>
<version>0.8.11</version>
<executions>
<execution>
<id>check</id>
<goals>
<goal>check</goal>
</goals>
<configuration>
<rules>
<rule>
<element>PACKAGE</element>
<limits>
<limit>
<counter>LINE</counter>
<value>COVEREDRATIO</value>
<minimum>0.80</minimum>
</limit>
<limit>
<counter>BRANCH</counter>
<value>COVEREDRATIO</value>
<minimum>0.75</minimum>
</limit>
</limits>
</rule>
</rules>
</configuration>
</execution>
</executions>
</plugin>

بررسی threshold:

bash
# اگر coverage کمتر از حد تعیین شده باشد، build fail می‌شود
mvn jacoco:check

# با detailed output
mvn jacoco:check -X

### 📑 Coverage Report در Formats مختلف:

bash
# XML format (برای CI/CD)
mvn jacoco:report
ls -lh target/site/jacoco/jacoco.xml

# CSV format (برای تحلیل)
cat target/site/jacoco/jacoco.csv

# HTML format (برای مشاهده)
# target/site/jacoco/index.html

---

## 8️⃣ Docker Build | استقرار Docker

### 🐳 ساخت Docker Image:

bash
# Build با Dockerfile موجود
docker build -t regular-reception:latest .

# با custom tag
docker build -t regular-reception:v1.0.0 .

# با build arguments
docker build --build-arg JAVA_VERSION=21 -t regular-reception:latest .

# بدون cache (clean build)
docker build --no-cache -t regular-reception:latest .

# مشاهده build progress
docker build --progress=plain -t regular-reception:latest .

# Multi-stage build (بهینه برای production)
docker build -f Dockerfile.multi -t regular-reception:prod .

**بررسی Image:**

bash
# لیست images
docker images | grep regular-reception

# Expected:
# regular-reception   latest   abc123def456   2 minutes ago   300MB

# بررسی layers
docker history regular-reception:latest

# بررسی حجم
docker images regular-reception:latest --format "{{.Size}}"

# بررسی با dive (اگر نصب باشد)
dive regular-reception:latest

# Inspect metadata
docker inspect regular-reception:latest | jq .

### 🚀 اجرا با Docker:

bash
# اجرای simple
docker run -d -p 8080:8080 --name regular-reception regular-reception:latest

# با environment variables
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  --name regular-reception \
  regular-reception:latest

# با volume برای logs
docker run -d -p 8080:8080 \
  -v $(pwd)/logs:/app/logs \
  --name regular-reception \
  regular-reception:latest

# Network mode برای ارتباط با postgres
docker run -d -p 8080:8080 \
  --network regularreception_default \
  -e DB_HOST=postgres \
  --name regular-reception \
  regular-reception:latest

# مشاهده logs
docker logs -f regular-reception

# توقف و حذف
docker stop regular-reception
docker rm regular-reception

### 🐘 Docker Compose - Full Stack:

bash
# شروع تمام سرویس‌ها (app + postgres + nginx)
docker-compose up -d

# Build و start
docker-compose up -d --build

# مشاهده status
docker-compose ps

# Expected output:
# NAME                          STATUS          PORTS
# regularreception-app-1        Up             0.0.0.0:8080->8080/tcp
# regularreception-postgres-1   Up             0.0.0.0:5432->5432/tcp
# regularreception-nginx-1      Up             0.0.0.0:80->80/tcp

# مشاهده logs
docker-compose logs -f

# فقط logs سرویس خاص
docker-compose logs -f app

# Restart یک سرویس
docker-compose restart app

# Scale یک سرویس (load balancing)
docker-compose up -d --scale app=3

# توقف همه
docker-compose down

# توقف و حذف volumes (⚠️ حذف دیتا)
docker-compose down -v

# Pull آخرین images
docker-compose pull

### 🔧 Docker Compose - Production:

bash
# استفاده از docker-compose.prod.yml
docker-compose -f docker-compose.prod.yml up -d

# Build برای production
docker-compose -f docker-compose.prod.yml build --no-cache

# مشاهده logs
docker-compose -f docker-compose.prod.yml logs -f

# Health check
curl http://localhost:8080/actuator/health

# Scale
docker-compose -f docker-compose.prod.yml up -d --scale app=2

### 💾 Database Backup with Docker:

bash
# Backup دیتابیس
docker exec regularreception-postgres-1 pg_dump -U postgres daryaftdb > backup_$(date +%Y%m%d_%H%M%S).sql

# Compressed backup
docker exec regularreception-postgres-1 pg_dump -U postgres daryaftdb | gzip > backup_$(date +%Y%m%d).sql.gz

# Restore از backup
cat backup_20250124.sql | docker exec -i regularreception-postgres-1 psql -U postgres daryaftdb

# Restore از compressed
gunzip -c backup_20250124.sql.gz | docker exec -i regularreception-postgres-1 psql -U postgres daryaftdb

### 🧹 Docker Cleanup:

bash
# حذف containers متوقف شده
docker container prune

# حذف images استفاده نشده
docker image prune -a

# حذف volumes استفاده نشده (⚠️ احتیاط)
docker volume prune

# پاکسازی کامل (⚠️ همه چیز حذف می‌شود)
docker system prune -a --volumes

# بررسی فضای استفاده شده
docker system df

# Expected:
# TYPE            TOTAL     ACTIVE    SIZE      RECLAIMABLE
# Images          5         2         1.2GB     800MB (66%)
# Containers      3         2         50MB      30MB (60%)
# Local Volumes   2         1         500MB     200MB (40%)

---

## 9️⃣ رفع مشکلات | Troubleshooting

### ❌ Build Failures:

bash
# مشکل: Maven dependency resolution error
# حل:
mvn dependency:purge-local-repository
mvn clean install -U

# مشکل: Compilation error
# حل: بررسی Java version
java -version
mvn -version

# تنظیم صحیح JAVA_HOME
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH

### ❌ Test Failures:

bash
# مشکل: TestContainers can't connect to Docker
# حل:
docker info
# اگر error داد، Docker service را restart کنید:
sudo systemctl restart docker  # Linux
# یا Docker Desktop را restart کنید

# مشکل: Database connection timeout
# حل:
docker-compose up -d postgres
docker-compose ps
# بررسی logs:
docker-compose logs postgres

# مشکل: Port already in use
# حل: پیدا کردن process
lsof -i :8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows
# Kill process
kill -9 <PID>

### ❌ Runtime Issues:

bash
# مشکل: Application won't start
# حل:
# 1. بررسی logs
tail -f logs/spring.log
# یا
docker-compose logs -f app

# 2. بررسی environment variables
env | grep DB_
env | grep JWT_

# 3. بررسی database connection
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb

# مشکل: Out of Memory
# حل: افزایش heap size
java -Xmx2g -Xms1g -jar target/regular-reception-0.0.1-SNAPSHOT.jar

# یا در Docker:
docker run -d -p 8080:8080 \
  -e JAVA_OPTS="-Xmx2g -Xms1g" \
  --name regular-reception \
  regular-reception:latest

# مشکل: Slow Performance
# حل:
# 1. بررسی connection pool
# 2. Enable query logging
# در application.yml:
logging:
  level:
org.hibernate.SQL: DEBUG
org.hibernate.type.descriptor.sql.BasicBinder: TRACE

# 3. Profile با JProfiler یا VisualVM

### ❌ Database Issues:

bash
# مشکل: Flyway migration failed
# حل:
# 1. بررسی flyway_schema_history
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb \
  -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"

# 2. Repair Flyway
mvn flyway:repair

# 3. در صورت نیاز، clean و rebuild
mvn flyway:clean
mvn flyway:migrate

# مشکل: Connection pool exhausted
# حل: تنظیم HikariCP در application.yml
spring:
  datasource:
hikari:
maximum-pool-size: 20
minimum-idle: 5
connection-timeout: 30000

# مشکل: Deadlock detected
# حل:
# 1. بررسی locks
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb \
  -c "SELECT * FROM pg_locks WHERE NOT granted;"

# 2. بررسی blocking queries
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb \
  -c "SELECT pid, usename, pg_blocking_pids(pid) as blocked_by, query 
FROM pg_stat_activity WHERE cardinality(pg_blocking_pids(pid)) > 0;"

### ❌ API Issues:

bash
# مشکل: 401 Unauthorized
# حل:
# 1. بررسی token validity
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq .

# 2. بررسی token expiration
# token را decode کنید در jwt.io

# 3. بررسی JWT_SECRET
echo $JWT_SECRET

# مشکل: 403 Forbidden
# حل: بررسی role و permissions
# در database:
docker exec -it regularreception-postgres-1 psql -U postgres -d daryaftdb \
  -c "SELECT u.username, r.name FROM users u JOIN roles r ON u.role_id = r.id;"

# مشکل: 429 Too Many Requests (Rate Limiting)
# حل:
# 1. بررسی تنظیمات rate limit
cat application.yml | grep -A 5 "rate-limit"

# 2. پاک کردن cache
curl -X POST http://localhost:8080/actuator/caches \
  -H "Authorization: Bearer $TOKEN"

# 3. Wait و retry
sleep 60
# سپس دوباره تلاش کنید

### ❌ Docker Issues:

bash
# مشکل: Container immediately stops
# حل:
docker logs regular-reception

# بررسی exit code
docker inspect regular-reception | jq '.[0].State.ExitCode'

# اجرای interactive برای debugging
docker run -it --rm regular-reception:latest /bin/bash

# مشکل: Can't connect to postgres from app
# حل:
# 1. بررسی network
docker network ls
docker network inspect regularreception_default

# 2. بررسی DNS resolution
docker exec regular-reception ping postgres

# 3. استفاده از صحیح host name
# در Docker Compose: DB_HOST=postgres
# خارج از Docker: DB_HOST=localhost

# مشکل: Volume permission denied
# حل:
chmod -R 777 ./data
# یا
docker run --user $(id -u):$(id -g) ...

### 🔍 Debug با Actuator Endpoints:

bash
# Health check
curl http://localhost:8080/actuator/health | jq .

# Environment
curl http://localhost:8080/actuator/env | jq .

# Metrics
curl http://localhost:8080/actuator/metrics | jq .

# Loggers
curl http://localhost:8080/actuator/loggers | jq .

# Thread dump
curl http://localhost:8080/actuator/threaddump

# Heap dump (⚠️ فایل بزرگ)
curl http://localhost:8080/actuator/heapdump --output heapdump.hprof

---

## 🔄 CI/CD Pipeline

### 📦 GitHub Actions Workflow:

ایجاد فایل `.github/workflows/build-test.yml`:

yaml
name: Build and Test

on:
  push:
branches: [ main, develop ]
  pull_request:
branches: [ main ]

jobs:
  build:
runs-on: ubuntu-latest

services:
postgres:
image: postgres:15
env:
POSTGRES_DB: daryaftdb_test
POSTGRES_USER: postgres
POSTGRES_PASSWORD: postgres123
ports:
- 5432:5432
options: >-
--health-cmd pg_isready
--health-interval 10s
--health-timeout 5s
--health-retries 5

steps:
- name: Checkout code
uses: actions/checkout@v3

- name: Set up JDK 21
uses: actions/setup-java@v3
with:
java-version: '21'
d

 - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Run Unit Tests
      run: mvn test
      env:
        DB_HOST: localhost
        DB_PORT: 5432
        DB_NAME: daryaftdb_test
        DB_USERNAME: postgres
        DB_PASSWORD: postgres123
    
    - name: Run Integration Tests
      run: mvn verify
      env:
        DB_HOST: localhost
        DB_PORT: 5432
        DB_NAME: daryaftdb_test
        DB_USERNAME: postgres
        DB_PASSWORD: postgres123
    
    - name: Generate Coverage Report
      run: mvn jacoco:report
    
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        files: ./target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella
        fail_ci_if_error: true
    
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2/repository
        key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
        restore-keys: |
          ${{ runner.os }}-maven-
    
    - name: Archive artifacts
      uses: actions/upload-artifact@v3
      with:
        name: jar-file
        path: target/*.jar
        retention-days: 7
    
    - name: Archive test reports
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-reports
        path: |
          target/surefire-reports
          target/site/jacoco
        retention-days: 7

  docker-build:
    needs: build
    runs-on: ubuntu-latest
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Set up Docker Buildx
      uses: docker/setup-buildx-action@v2
    
    - name: Login to Docker Hub
      uses: docker/login-action@v2
      with:
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}
    
    - name: Build and push Docker image
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: |
          yourusername/regular-reception:latest
          yourusername/regular-reception:${{ github.sha }}
        cache-from: type=registry,ref=yourusername/regular-reception:latest
        cache-to: type=inline

  security-scan:
    needs: build
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: Upload Trivy results to GitHub Security
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'

### 🔐 Setup GitHub Secrets:
```bash
# در تنظیمات GitHub Repository:
# Settings > Secrets and variables > Actions > New repository secret

# اضافه کردن secrets:
DOCKER_USERNAME=your_docker_username
DOCKER_PASSWORD=your_docker_password
POSTGRES_PASSWORD=secure_password_for_ci
JWT_SECRET=your_jwt_secret_for_testing

### 📊 GitLab CI/CD Pipeline:

ایجاد فایل `.gitlab-ci.yml`:

```yaml
stages:
  - build
  - test
  - package
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  DOCKER_IMAGE_NAME: "$CI_REGISTRY_IMAGE:$CI_COMMIT_REF_SLUG"

cache:
  paths:
    - .m2/repository

build:
  stage: build
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn clean compile
  artifacts:
    paths:
      - target/classes
    expire_in: 1 hour

test:unit:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  services:
    - postgres:15
  variables:
    POSTGRES_DB: daryaftdb_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres123
    DB_HOST: postgres
    DB_PORT: 5432
  script:
    - mvn test
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/surefire-reports
    expire_in: 1 week

test:integration:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  services:
    - postgres:15
  variables:
    POSTGRES_DB: daryaftdb_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres123
    DB_HOST: postgres
    DB_PORT: 5432
  script:
    - mvn verify
  artifacts:
    reports:
      junit: target/failsafe-reports/TEST-*.xml
    paths:
      - target/failsafe-reports
      - target/site/jacoco
    expire_in: 1 week

coverage:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn jacoco:report
    - cat target/site/jacoco/index.html
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    paths:
      - target/site/jacoco
    expire_in: 1 month

package:
  stage: package
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn package -DskipTests
  artifacts:
    paths:
      - target/*.jar
    expire_in: 1 month
  only:
    - main
    - develop

docker:build:
  stage: package
  image: docker:latest
  services:
    - docker:dind
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - docker build -t $DOCKER_IMAGE_NAME .
    - docker push $DOCKER_IMAGE_NAME
  only:
    - main

deploy:staging:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - mkdir -p ~/.ssh
    - echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
    - chmod 600 ~/.ssh/id_rsa
    - ssh-keyscan -H $STAGING_SERVER >> ~/.ssh/known_hosts
  script:
    - ssh $STAGING_USER@$STAGING_SERVER "docker pull $DOCKER_IMAGE_NAME"
    - ssh $STAGING_USER@$STAGING_SERVER "docker stop regular-reception || true"
    - ssh $STAGING_USER@$STAGING_SERVER "docker rm regular-reception || true"
    - ssh $STAGING_USER@$STAGING_SERVER "docker run -d -p 8080:8080 --name regular-reception $DOCKER_IMAGE_NAME"
  environment:
    name: staging
    url: https://staging.yourdomain.com
  only:
    - develop

deploy:production:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - mkdir -p ~/.ssh
    - echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
    - chmod 600 ~/.ssh/id_rsa
    - ssh-keyscan -H $PRODUCTION_SERVER >> ~/.ssh/known_hosts
  script:
    - ssh $PRODUCTION_USER@$PRODUCTION_SERVER "docker pull $DOCKER_IMAGE_NAME"
    - ssh $PRODUCTION_USER@$PRODUCTION_SERVER "cd /opt/regular-reception && docker-compose pull"
    - ssh $PRODUCTION_USER@$PRODUCTION_SERVER "cd /opt/regular-reception && docker-compose up -d"
  environment:
    name: production
    url: https://yourdomain.com
  when: manual
  only:
    - main

---

## ⚡ Performance Testing

### 🔧 Apache JMeter Setup:
```bash
# نصب JMeter
# Linux
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz
cd apache-jmeter-5.6.3/bin
./jmeter

# macOS
brew install jmeter

# Windows
# دانلود از: https://jmeter.apache.org/download_jmeter.cgi

### 📊 Load Testing Script:

ایجاد فایل `performance-test.jmx` یا اجرای از CLI:

```bash
# ساخت test plan
cat > load-test.jmx << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan>
      <stringProp name="TestPlan.comments">Load Test for RegularReception</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
EOF

# اجرای load test
jmeter -n -t load-test.jmx -l results.jtl -e -o report/

# مشاهده گزارش
open report/index.html

### 📈 K6 Load Testing (Modern Alternative):
```bash
# نصب K6
# Linux
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# macOS
brew install k6

# ایجاد test script
cat > load-test.js << 'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
{ duration: '30s', target: 20 },  // Ramp-up
{ duration: '1m', target: 50 },   // Stay at 50 users
{ duration: '30s', target: 0 },   // Ramp-down
  ],
  thresholds: {
http_req_duration: ['p(95)<500'], // 95% requests < 500ms
http_req_failed: ['rate<0.1'],    // <10% failure rate
  },
};

const BASE_URL = 'http://localhost:8080';

export function setup() {
  // Login و دریافت token
  const loginRes = http.post(`${BASE_URL}/api/auth/login`, 
JSON.stringify({
username: 'admin',
password: 'admin123'
}),
{ headers: { 'Content-Type': 'application/json' } }
  );
  
  return { token: loginRes.json('token') };
}

export default function(data) {
  const headers = {
'Authorization': `Bearer ${data.token}`,
'X-Business-Id': '1',
'Content-Type': 'application/json',
  };
  
  // Test 1: Get customers
  let res = http.get(`${BASE_URL}/api/customers`, { headers });
  check(res, {
'customers status 200': (r) => r.status === 200,
'customers response time < 200ms': (r) => r.timings.duration < 200,
  });
  
  sleep(1);
  
  // Test 2: Get subscriptions
  res = http.get(`${BASE_URL}/api/subscriptions?status=ACTIVE`, { headers });
  check(res, {
'subscriptions status 200': (r) => r.status === 200,
  });
  
  sleep(1);
  
  // Test 3: Create customer
  const payload = JSON.stringify({
firstName: `Test_${__VU}_${__ITER}`,
lastName: 'Customer',
phoneNumber: `09${Math.floor(Math.random() * 1000000000)}`,
nationalCode: `${Math.floor(Math.random() * 10000000000)}`,
  });
  
  res = http.post(`${BASE_URL}/api/customers`, payload, { headers });
  check(res, {
'create customer status 201': (r) => r.status === 201,
  });
  
  sleep(2);
}

export function teardown(data) {
  // Cleanup اگر لازم باشد
  console.log('Test completed');
}
EOF

# اجرای test
k6 run load-test.js

# اجرا با output به file
k6 run --out json=results.json load-test.js

# Cloud execution (اگر K6 Cloud account دارید)
k6 cloud load-test.js

### 📊 Gatling Load Testing:

```bash
# نصب Gatling
wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.10.3/gatling-charts-highcharts-bundle-3.10.3.zip
unzip gatling-charts-highcharts-bundle-3.10.3.zip
cd gatling-charts-highcharts-bundle-3.10.3

# ایجاد simulation
mkdir -p user-files/simulations
cat > user-files/simulations/RegularReceptionSimulation.scala << 'EOF'
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class RegularReceptionSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("RegularReception Load Test")
    .exec(
      http("Login")
        .post("/api/auth/login")
        .body(StringBody("""{"username":"admin","password":"admin123"}"""))
        .check(jsonPath("$.token").saveAs("token"))
    )
    .pause(1)
    .exec(
      http("Get Customers")
        .get("/api/customers")
        .header("Authorization", "Bearer ${token}")
        .header("X-Business-Id", "1")
        .check(status.is(200))
    )
    .pause(2)
    .exec(
      http("Get Subscriptions")
        .get("/api/subscriptions?status=ACTIVE")
        .header("Authorization", "Bearer ${token}")
        .header("X-Business-Id", "1")
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      rampUsers(100) during (30 seconds),
      constantUsersPerSec(20) during (1 minute)
    )
  ).protocols(httpProtocol)
}
EOF

# اجرای test
./bin/gatling.sh

# مشاهده گزارش
# در مسیر: results/

### 🔍 Application Performance Monitoring:
```bash
# استفاده از Spring Boot Actuator Metrics
curl http://localhost:8080/actuator/metrics | jq .

# بررسی metrics خاص:

# JVM Memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq .

# HTTP Requests
curl http://localhost:8080/actuator/metrics/http.server.requests | jq .

# Database Connection Pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active | jq .

# Tomcat Threads
curl http://localhost:8080/actuator/metrics/tomcat.threads.busy | jq .

# Custom metrics (اگر پیاده‌سازی شده باشد)
curl http://localhost:8080/actuator/metrics/custom.subscription.renewals | jq .

### 📈 Profiling با VisualVM:

```bash
# دانلود VisualVM
# از https://visualvm.github.io/download.html

# اجرای برنامه با JMX enabled
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.local.only=false \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar target/regular-reception-0.0.1-SNAPSHOT.jar

# اتصال از VisualVM به localhost:9010

---

## 📚 Additional Resources

### 📖 Documentation:

- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Spring Data JPA**: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- **PostgreSQL**: https://www.postgresql.org/docs/
- **Docker**: https://docs.docker.com/
- **Maven**: https://maven.apache.org/guides/

### 🎓 Best Practices:

1. **Always run tests before deployment**
2. **Use environment-specific configurations**
3. **Monitor application logs regularly**
4. **Keep dependencies up to date**
5. **Implement proper error handling**
6. **Use transactions appropriately**
7. **Optimize database queries**
8. **Implement caching where needed**
9. **Follow SOLID principles**
10. **Write clean, maintainable code**

### 🔧 Useful Commands Cheat Sheet:
```bash
# Maven
mvn clean install                    # Full build
mvn test                            # Run tests
mvn spring-boot:run                 # Run application
mvn package -DskipTests             # Build without tests
mvn dependency:tree                 # Show dependencies

# Docker
docker-compose up -d                # Start services
docker-compose down                 # Stop services
docker-compose logs -f app          # View logs
docker ps                           # List containers
docker exec -it <container> bash    # Enter container

# PostgreSQL
psql -U postgres -d daryaftdb       # Connect to DB
\dt                                 # List tables
\d customers                        # Describe table
SELECT * FROM customers LIMIT 10;   # Query

# Git
git status                          # Check status
git add .                           # Stage changes
git commit -m "message"             # Commit
git push origin main                # Push to remote
git pull origin main                # Pull from remote

# cURL API Testing
curl -X GET http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Business-Id: 1" | jq .

---

## 📞 Support & Contact

### 🐛 Reporting Issues:

برای گزارش مشکلات یا پیشنهادات:

1. **GitHub Issues**: https://github.com/nim3a/RegularReception/issues
2. **Email**: support@regularreception.com
3. **Documentation**: در فایل‌های `docs/` پروژه

### 🤝 Contributing:

برای مشارکت در پروژه:

1. Fork کردن repository
2. ایجاد feature branch
3. Commit تغییرات
4. Push به branch
5. ایجاد Pull Request

### 📄 License:

این پروژه تحت مجوز [MIT License](LICENSE) منتشر شده است.

---

**تاریخ آخرین بروزرسانی**: ۳ دی ۱۴۰۴ / ۲۴ دسامبر ۲۰۲۵  
**نسخه**: 1.0.0  
**نگهدارنده**: [nim3a](https://github.com/nim3a)

---

## ✅ Quick Start Checklist

- [ ] Java 21+ نصب شده
- [ ] Maven 3.9+ نصب شده
- [ ] Docker و Docker Compose نصب شده
- [ ] Repository کلون شده
- [ ] فایل `.env` تنظیم شده
- [ ] PostgreSQL با Docker راه‌اندازی شده
- [ ] `mvn clean install` با موفقیت اجرا شد
- [ ] تست‌ها با موفقیت پاس شدند
- [ ] برنامه اجرا شد و health check موفق بود
- [ ] API تست شد و پاسخ صحیح دریافت شد
- [ ] مستندات مطالعه شد

**🎉 تبریک! شما آماده توسعه هستید!**