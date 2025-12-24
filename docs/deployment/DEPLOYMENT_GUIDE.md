# Deployment Guide

## راهنمای استقرار RegularReception

این راهنما مراحل استقرار سیستم RegularReception در محیط‌های مختلف را شرح می‌دهد.

## 📋 پیش‌نیازها

### Development Environment
- Java 21 or higher
- Maven 3.9+
- PostgreSQL 15+ (یا Docker)
- Git

### Production Environment
- Docker & Docker Compose
- Nginx (optional)
- SSL Certificate
- Domain Name

## 🚀 استقرار محلی (Development)

### 1. نصب پیش‌نیازها

```bash
# نصب Java 21
# Windows: دانلود از adoptium.net
# Linux:
sudo apt update
sudo apt install openjdk-21-jdk

# بررسی نسخه
java -version
mvn -version
```

### 2. نصب PostgreSQL

#### استفاده از Docker (توصیه می‌شود)
```bash
cd docker
docker-compose up -d postgres
```

#### نصب مستقیم
```bash
# Ubuntu/Debian
sudo apt install postgresql-15

# macOS
brew install postgresql@15

# ایجاد دیتابیس
psql -U postgres
CREATE DATABASE regularreception;
CREATE USER admin WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE regularreception TO admin;
```

### 3. کلون کردن پروژه

```bash
git clone https://github.com/nim3a/RegularReception.git
cd RegularReception
```

### 4. تنظیمات محیطی

ایجاد فایل `.env`:

```bash
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=regularreception
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin123

# JWT
JWT_SECRET=your-super-secret-key-change-in-production

# SMS (optional)
SMS_USERNAME=demo
SMS_PASSWORD=demo
SMS_API_KEY=your-api-key
SMS_LINE_NUMBER=5000xxxx
```

### 5. اجرای برنامه

```bash
# نصب dependencies
mvn clean install

# اجرا در حالت development
mvn spring-boot:run

# یا ساخت JAR و اجرا
mvn clean package
java -jar target/daryaft-core-1.0.0.jar
```

### 6. دسترسی به برنامه

- **Backend API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **H2 Console**: http://localhost:8081/h2-console (فقط در dev)
- **Frontend**: http://localhost:8081/index.html

## 🐳 استقرار با Docker

### 1. ساخت Image

```bash
# ساخت backend image
docker build -t regularreception:latest .
```

### 2. اجرا با Docker Compose

```bash
cd docker

# اجرا در حالت development
docker-compose up -d

# اجرا در حالت production
docker-compose -f docker-compose.prod.yml up -d

# مشاهده logs
docker-compose logs -f backend

# متوقف کردن
docker-compose down
```

### 3. بررسی وضعیت

```bash
# لیست container‌ها
docker-compose ps

# بررسی health
curl http://localhost:8081/actuator/health
```

## 🌐 استقرار Production

### 1. تنظیمات امنیتی

#### ایجاد JWT Secret قوی
```bash
# تولید secret key قوی
openssl rand -base64 64
```

#### تنظیم متغیرهای محیطی
```bash
export JWT_SECRET="your-generated-secret-key"
export POSTGRES_PASSWORD="strong-password"
export SMS_API_KEY="your-api-key"
```

### 2. تنظیمات PostgreSQL

```bash
# ایجاد backup directory
mkdir -p /var/backups/postgres

# تنظیم postgresql.conf
max_connections = 100
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
```

### 3. پیکربندی Nginx

```nginx
# /etc/nginx/sites-available/regularreception
server {
    listen 80;
    server_name your-domain.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL Configuration
    ssl_certificate /etc/ssl/certs/your-cert.crt;
    ssl_certificate_key /etc/ssl/private/your-key.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Frontend
    location / {
        root /var/www/regularreception/frontend;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Swagger UI
    location /swagger-ui {
        proxy_pass http://localhost:8081;
    }

    # WebSocket support (if needed)
    location /ws {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

فعال‌سازی:
```bash
sudo ln -s /etc/nginx/sites-available/regularreception /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 4. SSL با Let's Encrypt

```bash
# نصب certbot
sudo apt install certbot python3-certbot-nginx

# دریافت certificate
sudo certbot --nginx -d your-domain.com

# تست auto-renewal
sudo certbot renew --dry-run
```

### 5. استقرار با Docker در Production

```bash
# کپی فایل‌ها به سرور
scp -r . user@your-server:/opt/regularreception/

# اتصال به سرور
ssh user@your-server

cd /opt/regularreception

# تنظیم environment variables
cp .env.example .env
nano .env  # ویرایش مقادیر

# اجرا
cd docker
docker-compose -f docker-compose.prod.yml up -d

# بررسی logs
docker-compose -f docker-compose.prod.yml logs -f
```

### 6. Systemd Service (اجرای مستقیم JAR)

ایجاد فایل `/etc/systemd/system/regularreception.service`:

```ini
[Unit]
Description=RegularReception Backend
After=postgresql.service

[Service]
Type=simple
User=regularreception
WorkingDirectory=/opt/regularreception
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod target/daryaft-core-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=regularreception

Environment="POSTGRES_HOST=localhost"
Environment="POSTGRES_PORT=5432"
Environment="POSTGRES_DB=regularreception"
Environment="POSTGRES_USER=admin"
Environment="POSTGRES_PASSWORD=your-password"
Environment="JWT_SECRET=your-secret"

[Install]
WantedBy=multi-user.target
```

فعال‌سازی:
```bash
sudo systemctl daemon-reload
sudo systemctl enable regularreception
sudo systemctl start regularreception
sudo systemctl status regularreception

# مشاهده logs
sudo journalctl -u regularreception -f
```

## 📊 Monitoring & Logging

### 1. Application Logs

```bash
# Docker logs
docker-compose logs -f backend

# Systemd logs
sudo journalctl -u regularreception -f

# فایل لاگ (اگر پیکربندی شده باشد)
tail -f /var/log/regularreception/application.log
```

### 2. Health Check Endpoint

```bash
# بررسی سلامت برنامه
curl http://localhost:8081/actuator/health

# بررسی metrics
curl http://localhost:8081/actuator/metrics
```

### 3. Database Monitoring

```bash
# اتصال به PostgreSQL
docker-compose exec postgres psql -U admin -d regularreception

# بررسی connections
SELECT * FROM pg_stat_activity;

# بررسی سایز دیتابیس
SELECT pg_size_pretty(pg_database_size('regularreception'));
```

## 💾 Backup & Restore

### Backup

```bash
# Backup دیتابیس
docker-compose exec postgres pg_dump -U admin regularreception > backup_$(date +%Y%m%d).sql

# یا با استفاده از اسکریپت
./scripts/backup.sh
```

### Restore

```bash
# Restore از backup
docker-compose exec -T postgres psql -U admin regularreception < backup_20251224.sql

# یا با استفاده از اسکریپت
./scripts/restore.sh backup_20251224.sql
```

## 🔄 Updates & Migrations

### 1. آپدیت برنامه

```bash
# Pull latest code
git pull origin main

# ساخت مجدد
mvn clean package

# Restart service
sudo systemctl restart regularreception

# یا با Docker
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d --build
```

### 2. Database Migrations

Flyway به صورت خودکار migration‌ها را اجرا می‌کند:

```bash
# بررسی وضعیت migrations
mvn flyway:info

# اجرای migrations
mvn flyway:migrate

# Rollback (در صورت نیاز)
mvn flyway:undo
```

## 🔍 Troubleshooting

### مشکلات رایج

#### 1. برنامه start نمی‌شود

```bash
# بررسی logs
docker-compose logs backend

# بررسی port در حال استفاده
netstat -tulpn | grep 8081

# بررسی دیتابیس
docker-compose ps postgres
docker-compose exec postgres psql -U admin -d regularreception
```

#### 2. Connection به دیتابیس

```bash
# بررسی دسترسی
psql -h localhost -p 5432 -U admin -d regularreception

# بررسی متغیرهای محیطی
docker-compose exec backend env | grep POSTGRES
```

#### 3. Authentication Issues

```bash
# بررسی JWT_SECRET
docker-compose exec backend env | grep JWT

# تست endpoint
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 📱 Post-Deployment Checklist

- [ ] تست تمام API endpoints با Swagger
- [ ] بررسی database migrations
- [ ] تنظیم backup خودکار
- [ ] پیکربندی monitoring & alerting
- [ ] تست SSL certificate
- [ ] بررسی logs
- [ ] تست authentication & authorization
- [ ] بررسی performance
- [ ] تنظیم firewall rules
- [ ] مستندسازی credentials

## 🆘 Support

برای مشکلات و سوالات:
- مستندات: [docs/](../)
- Issues: [GitHub Issues](https://github.com/nim3a/RegularReception/issues)
- Email: support@example.com

## 📚 منابع بیشتر

- [BUILD_AND_TEST.md](../development/BUILD_AND_TEST.md)
- [API_REFERENCE.md](../api/API_REFERENCE.md)
- [SYSTEM_ARCHITECTURE.md](../architecture/SYSTEM_ARCHITECTURE.md)
