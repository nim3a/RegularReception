# RegularReception

> 🚀 Enterprise Subscription & Payment Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

یک سیستم جامع و حرفه‌ای برای مدیریت مشتریان، اشتراک‌ها و پرداخت‌های دوره‌ای با پشتیبانی کامل از زبان فارسی.

## ✨ ویژگی‌های اصلی

- ✅ **مدیریت مشتری** - سیستم CRM کامل با امکان جستجو و فیلترینگ
- ✅ **سیستم اشتراک** - پلن‌های ماهانه/سالانه با مدیریت خودکار انقضا
- ✅ **پردازش پرداخت** - درگاه پرداخت Mock با قابلیت توسعه
- ✅ **اعلان SMS** - یادآورهای خودکار از طریق ملی‌پیامک
- ✅ **Multi-tenancy** - پشتیبانی از چندین کسب‌وکار
- ✅ **احراز هویت JWT** - امنیت سطح Enterprise
- ✅ **وظایف زمان‌بندی‌شده** - بررسی خودکار اشتراک‌ها و یادآورها
- ✅ **پشتیبانی فارسی/Persian** - UI راست‌چین و Localization کامل
- ✅ **REST API** - مستندات کامل با Swagger UI
- ✅ **Docker Support** - استقرار آسان با Docker Compose

## 📁 ساختار پروژه

```
RegularReception/
├── src/                       # کد منبع Spring Boot
│   ├── main/
│   │   ├── java/              # Java source files
│   │   └── resources/         # تنظیمات و migrations
│   └── test/                  # تست‌های Unit و Integration
│
├── frontend/                  # فایل‌های Frontend
│   └── public/                # HTML, CSS, JavaScript
│       ├── index.html         # صفحه اصلی
│       ├── dashboard.html     # داشبورد مدیریت
│       └── payment-gateway.html
│
├── docs/                      # مستندات کامل
│   ├── api/                   # API Documentation
│   ├── deployment/            # راهنمای استقرار
│   ├── development/           # راهنمای توسعه
│   └── architecture/          # معماری سیستم
│
├── docker/                    # Docker configurations
│   ├── docker-compose.yml     # Development setup
│   ├── docker-compose.prod.yml
│   └── nginx/                 # Nginx config
│
├── scripts/                   # اسکریپت‌های استقرار
│   ├── backup.sh              # Backup دیتابیس
│   ├── deploy.sh              # استقرار خودکار
│   ├── monitor.sh             # نظارت بر سیستم
│   └── restore.sh             # بازیابی از backup
│
├── database-migrations/       # SQL migrations
├── data/                      # داده‌های نمونه
├── pom.xml                    # Maven dependencies
└── README.md                  # همین فایل
```

## 🚀 شروع سریع

### پیش‌نیازها

- **Java 21+** (Eclipse Temurin یا OpenJDK)
- **Maven 3.9+**
- **Docker & Docker Compose** (اختیاری اما توصیه می‌شود)
- **PostgreSQL 15+** (یا استفاده از Docker)

### نصب و راه‌اندازی

#### 1️⃣ کلون کردن پروژه

```bash
git clone https://github.com/nim3a/RegularReception.git
cd RegularReception
```

#### 2️⃣ راه‌اندازی دیتابیس

**با استفاده از Docker (توصیه می‌شود):**

```bash
cd docker
docker-compose up -d postgres
```

**یا نصب مستقیم PostgreSQL:**

```bash
# ایجاد دیتابیس
psql -U postgres
CREATE DATABASE regularreception;
CREATE USER admin WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE regularreception TO admin;
```

#### 3️⃣ اجرای برنامه

```bash
# نصب dependencies
mvn clean install

# اجرا در حالت development
mvn spring-boot:run
```

یا ساخت JAR و اجرا:

```bash
mvn clean package
java -jar target/daryaft-core-1.0.0.jar
```

#### 4️⃣ دسترسی به برنامه

- **🌐 Backend API**: http://localhost:8081
- **📚 Swagger UI**: http://localhost:8081/swagger-ui.html
- **🎨 Frontend**: http://localhost:8081/index.html
- **💾 H2 Console**: http://localhost:8081/h2-console (فقط در dev)

## 🐳 استقرار با Docker

### تمام سیستم با یک دستور:

```bash
cd docker
docker-compose up -d
```

این دستور موارد زیر را راه‌اندازی می‌کند:
- **PostgreSQL** database (port 5432)
- **Backend** application (port 8081)
- **Nginx** web server (port 80)

### مشاهده وضعیت:

```bash
# بررسی containers
docker-compose ps

# مشاهده logs
docker-compose logs -f backend

# متوقف کردن
docker-compose down
```

## 📖 مستندات

### راهنماهای کامل:

| راهنما | توضیحات | لینک |
|--------|---------|------|
| **API Reference** | مستندات کامل REST API | [📚 API_REFERENCE.md](docs/api/API_REFERENCE.md) |
| **Deployment Guide** | راهنمای استقرار Production | [🚀 DEPLOYMENT_GUIDE.md](docs/deployment/DEPLOYMENT_GUIDE.md) |
| **Build & Test** | راهنمای ساخت و تست | [🛠️ BUILD_AND_TEST.md](docs/development/BUILD_AND_TEST.md) |
| **System Architecture** | معماری سیستم | [🏗️ SYSTEM_ARCHITECTURE.md](docs/architecture/SYSTEM_ARCHITECTURE.md) |
| **Contributing** | راهنمای مشارکت | [🤝 CONTRIBUTING.md](docs/development/CONTRIBUTING.md) |

### Swagger UI (مستندات تعاملی):

برای تست و مشاهده تعاملی APIها:

```
http://localhost:8081/swagger-ui.html
```

## 🧪 تست

### اجرای تمام تست‌ها:

```bash
# تست‌های Unit + Integration
mvn test

# با گزارش Coverage
mvn clean verify jacoco:report

# مشاهده گزارش Coverage
start target/site/jacoco/index.html    # Windows
open target/site/jacoco/index.html     # macOS/Linux
```

### انواع تست‌ها:

- ✅ **Unit Tests** - Service layer با Mockito
- ✅ **Integration Tests** - Controller + Repository با TestContainers
- ✅ **E2E Tests** - سناریوهای کامل workflow
- ✅ **Security Tests** - Authentication & Authorization

## 📊 Stack فناوری

### Backend:
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 21
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15 (Production), H2 (Development)
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **API Docs**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build Tool**: Maven 3.9+

### Frontend:
- **HTML5, CSS3, JavaScript**
- **Bootstrap 5** (UI Framework)
- **Chart.js** (نمودارها و گزارشات)

### Infrastructure:
- **Container**: Docker
- **Orchestration**: Docker Compose
- **Web Server**: Nginx
- **CI/CD**: GitHub Actions (planned)

## 🔐 امنیت

- **JWT-based Authentication** - احراز هویت مبتنی بر توکن
- **Role-based Access Control (RBAC)** - کنترل دسترسی نقش‌محور
- **Password Encryption** - رمزنگاری با BCrypt
- **Multi-tenant Data Isolation** - جداسازی داده‌های کسب‌وکارها
- **CORS Configuration** - تنظیمات امنیتی Cross-Origin
- **Rate Limiting** - محدودیت تعداد درخواست (planned)

## 🔄 ویژگی‌های اضافی

### وظایف زمان‌بندی‌شده:

- **بررسی اشتراک‌های عقب‌افتاده** - روزانه ساعت 2 صبح
- **بررسی اشتراک‌های منقضی** - روزانه ساعت 3 صبح
- **یادآوری پرداخت** - روزانه ساعت 9 صبح (3 روز قبل)
- **پردازش SMS‌های صف** - هر 5 دقیقه

### سرویس SMS:

- **ارائه‌دهنده**: ملی‌پیامک (Melipayamak)
- **قابلیت‌ها**: یادآوری، تأیید پرداخت، اعلان‌ها
- **قالب‌های آماده**: پیام‌های فارسی از پیش تعریف‌شده

### درگاه پرداخت:

- **Mock Gateway** برای توسعه و تست
- **قابل توسعه** برای اتصال به درگاه‌های واقعی
- **پشتیبانی از**: نقدی، آنلاین، چک

## 🌍 محیط‌های مختلف

### Development:
```bash
# استفاده از H2 database
mvn spring-boot:run
```

### Production:
```bash
# استفاده از PostgreSQL
java -jar target/daryaft-core-1.0.0.jar --spring.profiles.active=prod
```

### تنظیمات محیطی:

متغیرهای محیطی مهم:

```bash
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=regularreception
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123

# Security
JWT_SECRET=your-super-secret-key-here-change-in-production
JWT_EXPIRATION=86400000

# SMS (optional)
SMS_USERNAME=your-username
SMS_PASSWORD=your-password
SMS_API_KEY=your-api-key
SMS_LINE_NUMBER=5000xxxx
```

## 📈 Roadmap

### آینده نزدیک:
- [ ] پنل ادمین پیشرفته
- [ ] گزارشات مالی تفصیلی
- [ ] اتصال به درگاه‌های واقعی پرداخت
- [ ] نوتیفیکیشن Push
- [ ] اپلیکیشن موبایل (React Native)

### بلندمدت:
- [ ] معماری Microservices
- [ ] Redis Caching
- [ ] Elasticsearch برای جستجو
- [ ] GraphQL API
- [ ] Real-time Dashboard با WebSocket

## 🤝 مشارکت

مشارکت شما در توسعه این پروژه بسیار ارزشمند است!

1. Fork کنید
2. یک branch جدید بسازید (`git checkout -b feature/AmazingFeature`)
3. تغییرات خود را commit کنید (`git commit -m 'feat: Add AmazingFeature'`)
4. Push کنید (`git push origin feature/AmazingFeature`)
5. یک Pull Request باز کنید

برای جزئیات بیشتر: [CONTRIBUTING.md](docs/development/CONTRIBUTING.md)

## 📝 مجوز (License)

این پروژه تحت مجوز MIT منتشر شده است - برای جزئیات فایل [LICENSE](LICENSE) را ببینید.

## 👤 نویسنده

**Nim3a**

- GitHub: [@nim3a](https://github.com/nim3a)
- Email: nim3a@example.com

## 🙏 تشکر و قدردانی

این پروژه با استفاده از فناوری‌های زیر ساخته شده:

- [Spring Boot](https://spring.io/projects/spring-boot)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/)
- [Bootstrap](https://getbootstrap.com/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

## 📞 پشتیبانی

اگر سوال یا مشکلی دارید:

- 📧 **Email**: support@regularreception.com
- 🐛 **Issues**: [GitHub Issues](https://github.com/nim3a/RegularReception/issues)
- 📖 **Docs**: [مستندات کامل](docs/)

## ⭐ Star این پروژه

اگر این پروژه برای شما مفید بود، لطفاً یک ⭐ بدهید!

---

**نسخه:** 1.0.0  
**آخرین بروزرسانی:** دی ۱۴۰۴ / دسامبر ۲۰۲۵

**ساخته شده با ❤️ در ایران**
