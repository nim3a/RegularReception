# 🚀 Deployment Guide | راهنمای استقرار

Complete deployment guide for Daryaft Core Application in English and Persian.

راهنمای کامل استقرار برنامه دریافت کُر به زبان‌های انگلیسی و فارسی.

---

## Table of Contents | فهرست مطالب

- [English Documentation](#english-documentation)
  - [Prerequisites](#prerequisites)
  - [Initial Setup](#initial-setup)
  - [Development Deployment](#development-deployment)
  - [Production Deployment](#production-deployment)
  - [Database Backup](#database-backup)
  - [Monitoring](#monitoring)
  - [SSL Configuration](#ssl-configuration)
  - [Troubleshooting](#troubleshooting)
  - [Security Checklist](#security-checklist)
  - [Maintenance](#maintenance)
- [مستندات فارسی](#مستندات-فارسی)
  - [پیش‌نیازها](#پیش‌نیازها)
  - [راه‌اندازی اولیه](#راه‌اندازی-اولیه)
  - [استقرار توسعه](#استقرار-توسعه)
  - [استقرار تولید](#استقرار-تولید)
  - [پشتیبان‌گیری پایگاه داده](#پشتیبان‌گیری-پایگاه-داده)
  - [نظارت](#نظارت)
  - [پیکربندی SSL](#پیکربندی-ssl)
  - [رفع مشکلات](#رفع-مشکلات)
  - [چک‌لیست امنیتی](#چک‌لیست-امنیتی)
  - [نگهداری](#نگهداری)

---

# English Documentation

## Prerequisites

### 📋 Required Software

Before deploying the application, ensure you have the following installed:

- **Docker** 24.0+ - Container runtime
- **Docker Compose** 2.20+ - Multi-container orchestration
- **Git** - Version control
- **Bash/PowerShell** - Script execution

### 💻 System Requirements

#### Minimum Requirements (Development)
- **CPU**: 2 cores
- **RAM**: 4 GB
- **Disk Space**: 10 GB free space
- **OS**: Linux, macOS, or Windows 10+ with WSL2

#### Recommended Requirements (Production)
- **CPU**: 4+ cores
- **RAM**: 8 GB
- **Disk Space**: 50 GB free space (including logs and backups)
- **OS**: Ubuntu 20.04+ LTS or similar Linux distribution
- **Network**: Static IP address with open ports 80, 443

### 🔍 Verify Installation

Check that all prerequisites are installed:

```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker compose version

# Check Git version
git --version
```

---

## Initial Setup

### 1️⃣ Clone Repository

```bash
# Clone the repository
git clone https://github.com/yourusername/daryaft-core.git

# Navigate to project directory
cd daryaft-core
```

### 2️⃣ Create Environment Configuration

Create a `.env` file from the template:

```bash
# Copy environment template
cp .env.example .env
```

Edit the `.env` file with your configuration:

```bash
# Database Configuration
POSTGRES_DB=daryaft_db
POSTGRES_USER=daryaft_user
POSTGRES_PASSWORD=change_this_secure_password

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here_min_256_bits
JWT_EXPIRATION=86400000

# SMS Configuration
SMS_API_KEY=your_sms_api_key
SMS_SENDER=your_sender_number

# Rate Limiting
RATE_LIMIT_REQUESTS_PER_MINUTE=60
RATE_LIMIT_BURST_CAPACITY=100

# Server Configuration
SERVER_PORT=8080
NGINX_HTTP_PORT=80
NGINX_HTTPS_PORT=443
```

### 3️⃣ Make Scripts Executable

```bash
# Make deployment scripts executable (Linux/macOS)
chmod +x scripts/*.sh

# For Windows, scripts will run with PowerShell/Git Bash
```

---

## Development Deployment

### 🛠️ Start Development Environment

```bash
# Start all services with Docker Compose
docker compose up -d

# View logs
docker compose logs -f
```

### 🌐 Access Application

- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Landing Page**: `http://localhost/landing.html`
- **Dashboard**: `http://localhost/dashboard.html`

### 🔑 Default Credentials

```
Username: admin@daryaft.com
Password: Admin123!
```

**⚠️ Important**: Change default credentials immediately after first login!

### 🛑 Stop Development Environment

```bash
# Stop all services
docker compose down

# Stop and remove volumes (⚠️ deletes data)
docker compose down -v
```

---

## Production Deployment

### 1️⃣ Configure Environment

Ensure your `.env` file has production-ready settings:

```bash
# Set production profile
SPRING_PROFILES_ACTIVE=prod

# Use strong passwords
POSTGRES_PASSWORD=very_strong_random_password_here

# Use secure JWT secret (minimum 256 bits)
JWT_SECRET=generate_random_secure_key_using_openssl_rand_base64_32

# Configure external URLs
APP_BASE_URL=https://yourdomain.com
```

### 2️⃣ Run Deployment Script

```bash
# Linux/macOS
./scripts/deploy.sh

# Windows (Git Bash)
bash scripts/deploy.sh

# Or use Docker Compose directly
docker compose -f docker-compose.prod.yml up -d
```

### 3️⃣ Verify Deployment

```bash
# Check running containers
docker ps

# Check application logs
docker compose -f docker-compose.prod.yml logs -f app

# Test health endpoint
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### 4️⃣ Configure Reverse Proxy

The application runs behind Nginx. Ensure nginx configuration is correct:

```bash
# Test nginx configuration
docker compose exec nginx nginx -t

# Reload nginx if config changes
docker compose exec nginx nginx -s reload
```

---

## Database Backup

### 💾 Create Backup

```bash
# Using the backup script (Linux/macOS)
./scripts/backup.sh

# Manual backup with Docker
docker compose exec postgres pg_dump -U daryaft_user daryaft_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Compressed backup (recommended)
docker compose exec postgres pg_dump -U daryaft_user daryaft_db | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### 📥 Restore from Backup

```bash
# Using the restore script (Linux/macOS)
./scripts/restore.sh backup_20231223_120000.sql

# Manual restore
docker compose exec -T postgres psql -U daryaft_user daryaft_db < backup_20231223_120000.sql

# Restore from compressed backup
gunzip < backup_20231223_120000.sql.gz | docker compose exec -T postgres psql -U daryaft_user daryaft_db
```

### 📁 Backup Location and Retention

- **Default Location**: `./data/backups/`
- **Recommended Retention**: 
  - Daily backups: Keep last 7 days
  - Weekly backups: Keep last 4 weeks
  - Monthly backups: Keep last 12 months

```bash
# Automated backup with cron (add to crontab)
0 2 * * * cd /path/to/daryaft-core && ./scripts/backup.sh >> /var/log/daryaft-backup.log 2>&1
```

---

## Monitoring

### 📊 System Status Check

```bash
# Check all container status
docker compose ps

# Check resource usage
docker stats

# Using monitoring script (Linux/macOS)
./scripts/monitor.sh
```

### 📝 View Logs

```bash
# View all logs
docker compose logs

# Follow logs in real-time
docker compose logs -f

# View specific service logs
docker compose logs -f app
docker compose logs -f postgres
docker compose logs -f nginx

# View last 100 lines
docker compose logs --tail=100 app
```

### 🏥 Health Check Endpoints

The application provides several health check endpoints:

```bash
# Main health check
curl http://localhost:8080/actuator/health

# Detailed health information
curl http://localhost:8080/actuator/health/detailed

# Database health
curl http://localhost:8080/actuator/health/db

# Disk space
curl http://localhost:8080/actuator/health/diskSpace
```

### 📈 Metrics Endpoints

```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# JVM memory metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP requests metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## SSL Configuration

### 🔐 Obtain SSL Certificate (Let's Encrypt)

```bash
# Install certbot
sudo apt-get update
sudo apt-get install certbot

# Stop nginx temporarily
docker compose stop nginx

# Obtain certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Certificates will be saved to:
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

### 📋 Copy Certificates

```bash
# Create SSL directory
mkdir -p nginx/ssl

# Copy certificates
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem nginx/ssl/

# Set appropriate permissions
sudo chmod 644 nginx/ssl/fullchain.pem
sudo chmod 600 nginx/ssl/privkey.pem
```

### ⚙️ Update Nginx Configuration

Edit `nginx/nginx.conf` to enable SSL:

```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Rest of configuration...
}

server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

### 🔄 Restart Nginx

```bash
# Test configuration
docker compose exec nginx nginx -t

# Restart nginx
docker compose restart nginx
```

### 🔁 Auto-Renewal Setup

```bash
# Add to crontab for automatic renewal
sudo crontab -e

# Add this line (renews every day at 2:30 AM)
30 2 * * * certbot renew --quiet --post-hook "docker compose restart nginx"
```

---

## Troubleshooting

### ❌ Application Won't Start

**Symptoms**: Container exits immediately or won't start

**Solutions**:

```bash
# Check container logs
docker compose logs app

# Check if port is already in use
netstat -tuln | grep 8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows

# Remove and recreate containers
docker compose down
docker compose up -d

# Check environment variables
docker compose config

# Rebuild containers
docker compose up -d --build
```

### 🐌 High Memory Usage

**Symptoms**: Application is slow, container using excessive memory

**Solutions**:

```bash
# Check memory usage
docker stats

# Adjust JVM heap size in docker-compose.yml
services:
  app:
    environment:
      - JAVA_OPTS=-Xmx2g -Xms1g

# Restart application
docker compose restart app

# Check for memory leaks in logs
docker compose logs app | grep -i "OutOfMemory"
```

### 🔌 Database Connection Issues

**Symptoms**: "Connection refused" or "Connection timeout" errors

**Solutions**:

```bash
# Check if database container is running
docker compose ps postgres

# Check database logs
docker compose logs postgres

# Test database connectivity
docker compose exec app nc -zv postgres 5432

# Verify database credentials in .env file
docker compose exec postgres psql -U daryaft_user -d daryaft_db -c "SELECT version();"

# Check network connectivity
docker network inspect regularreception_default

# Restart database
docker compose restart postgres
```

### 🌐 Nginx/Proxy Issues

**Symptoms**: 502 Bad Gateway, connection issues

**Solutions**:

```bash
# Check nginx logs
docker compose logs nginx

# Test nginx configuration
docker compose exec nginx nginx -t

# Check if app is reachable from nginx
docker compose exec nginx curl http://app:8080/actuator/health

# Restart nginx
docker compose restart nginx
```

### 💾 Disk Space Issues

**Symptoms**: Application crashes, backup failures

**Solutions**:

```bash
# Check disk usage
df -h

# Check Docker disk usage
docker system df

# Clean up Docker resources
docker system prune -a --volumes

# Remove old log files
find ./data/logs -name "*.log" -mtime +30 -delete

# Clean Maven build cache
docker compose exec app mvn clean
```

### 🔍 Check Connectivity

```bash
# Test external connectivity
docker compose exec app curl -I https://google.com

# Check DNS resolution
docker compose exec app nslookup google.com

# Check internal container communication
docker compose exec app curl http://postgres:5432
```

---

## Security Checklist

### ✅ Pre-Deployment Security

- [ ] **Change JWT Secret**
  ```bash
  # Generate secure JWT secret
  openssl rand -base64 32
  # Add to .env file
  JWT_SECRET=generated_secret_here
  ```

- [ ] **Strong Database Password**
  ```bash
  # Generate strong password
  openssl rand -base64 24
  # Update in .env file
  POSTGRES_PASSWORD=generated_password_here
  ```

- [ ] **Enable HTTPS**
  - [ ] Obtain SSL certificate (Let's Encrypt or commercial)
  - [ ] Configure nginx with SSL
  - [ ] Force HTTPS redirect
  - [ ] Enable HSTS header

- [ ] **Configure Firewall**
  ```bash
  # Ubuntu/Debian with ufw
  sudo ufw allow 22/tcp   # SSH
  sudo ufw allow 80/tcp   # HTTP
  sudo ufw allow 443/tcp  # HTTPS
  sudo ufw enable
  
  # Check firewall status
  sudo ufw status
  ```

- [ ] **Enable Rate Limiting**
  - [ ] Configure rate limits in application.yml
  - [ ] Set appropriate limits in .env
  - [ ] Test rate limiting is working

- [ ] **Regular Backups**
  - [ ] Set up automated backup cron job
  - [ ] Test backup restoration procedure
  - [ ] Store backups in secure location
  - [ ] Implement backup retention policy

- [ ] **Log Rotation**
  ```bash
  # Create logrotate configuration
  sudo nano /etc/logrotate.d/daryaft
  
  # Add configuration:
  /path/to/daryaft-core/data/logs/*.log {
      daily
      rotate 14
      compress
      delaycompress
      missingok
      notifempty
      create 0644 root root
  }
  ```

- [ ] **Update Dependencies**
  ```bash
  # Check for Maven dependency updates
  docker compose exec app mvn versions:display-dependency-updates
  
  # Update Docker images
  docker compose pull
  ```

### 🔒 Additional Security Measures

- [ ] Change default admin credentials
- [ ] Implement 2FA for admin accounts
- [ ] Regular security audits
- [ ] Monitor for suspicious activity
- [ ] Keep application and dependencies updated
- [ ] Implement database encryption at rest
- [ ] Use secrets management (e.g., Docker secrets, HashiCorp Vault)
- [ ] Regular penetration testing
- [ ] Implement Web Application Firewall (WAF)
- [ ] Enable audit logging

---

## Maintenance

### 🔄 Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and restart services
docker compose down
docker compose up -d --build

# Or use deployment script
./scripts/deploy.sh

# Verify update
docker compose logs -f app
```

### 🧹 Clean Docker Images

```bash
# Remove unused images
docker image prune -a

# Remove stopped containers
docker container prune

# Remove unused volumes (⚠️ careful with data)
docker volume prune

# Complete cleanup (⚠️ removes everything)
docker system prune -a --volumes

# Check disk space saved
docker system df
```

### 🗄️ Database Maintenance

```bash
# Vacuum database (reclaim space)
docker compose exec postgres psql -U daryaft_user daryaft_db -c "VACUUM VERBOSE;"

# Analyze database (update statistics)
docker compose exec postgres psql -U daryaft_user daryaft_db -c "ANALYZE VERBOSE;"

# Reindex database
docker compose exec postgres psql -U daryaft_user daryaft_db -c "REINDEX DATABASE daryaft_db;"

# Check database size
docker compose exec postgres psql -U daryaft_user daryaft_db -c "SELECT pg_size_pretty(pg_database_size('daryaft_db'));"
```

### 📊 Performance Optimization

```bash
# Check slow queries
docker compose exec postgres psql -U daryaft_user daryaft_db -c "SELECT query, mean_exec_time FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;"

# Optimize tables
docker compose exec postgres psql -U daryaft_user daryaft_db -c "VACUUM ANALYZE;"

# Check index usage
docker compose exec postgres psql -U daryaft_user daryaft_db -c "SELECT schemaname, tablename, indexname, idx_scan FROM pg_stat_user_indexes ORDER BY idx_scan;"
```

### 🔍 Regular Health Checks

```bash
# Create monitoring script
cat > check_health.sh << 'EOF'
#!/bin/bash
HEALTH_URL="http://localhost:8080/actuator/health"
RESPONSE=$(curl -s $HEALTH_URL | jq -r '.status')

if [ "$RESPONSE" == "UP" ]; then
    echo "✅ Application is healthy"
    exit 0
else
    echo "❌ Application is unhealthy"
    exit 1
fi
EOF

chmod +x check_health.sh

# Add to cron for regular checks
*/5 * * * * /path/to/check_health.sh >> /var/log/daryaft-health.log 2>&1
```

---

# مستندات فارسی

## پیش‌نیازها

### 📋 نرم‌افزارهای مورد نیاز

قبل از استقرار برنامه، اطمینان حاصل کنید که موارد زیر نصب شده‌اند:

- **Docker** نسخه 24.0 یا بالاتر - محیط اجرای کانتینر
- **Docker Compose** نسخه 2.20 یا بالاتر - مدیریت کانتینرهای چندگانه
- **Git** - کنترل نسخه
- **Bash/PowerShell** - اجرای اسکریپت‌ها

### 💻 نیازمندی‌های سیستم

#### حداقل نیازمندی‌ها (محیط توسعه)
- **پردازنده**: 2 هسته
- **حافظه RAM**: 4 گیگابایت
- **فضای دیسک**: 10 گیگابایت فضای خالی
- **سیستم‌عامل**: Linux، macOS یا Windows 10+ با WSL2

#### نیازمندی‌های پیشنهادی (محیط تولید)
- **پردازنده**: 4 هسته یا بیشتر
- **حافظه RAM**: 8 گیگابایت
- **فضای دیسک**: 50 گیگابایت فضای خالی (شامل لاگ‌ها و پشتیبان‌ها)
- **سیستم‌عامل**: Ubuntu 20.04+ LTS یا توزیع مشابه Linux
- **شبکه**: آدرس IP ثابت با پورت‌های 80 و 443 باز

### 🔍 بررسی نصب

بررسی کنید که تمام پیش‌نیازها نصب شده‌اند:

```bash
# بررسی نسخه Docker
docker --version

# بررسی نسخه Docker Compose
docker compose version

# بررسی نسخه Git
git --version
```

---

## راه‌اندازی اولیه

### 1️⃣ دریافت مخزن

```bash
# دریافت مخزن
git clone https://github.com/yourusername/daryaft-core.git

# ورود به دایرکتوری پروژه
cd daryaft-core
```

### 2️⃣ ایجاد تنظیمات محیطی

ایجاد فایل `.env` از الگو:

```bash
# کپی کردن الگوی محیطی
cp .env.example .env
```

ویرایش فایل `.env` با تنظیمات شما:

```bash
# تنظیمات پایگاه داده
POSTGRES_DB=daryaft_db
POSTGRES_USER=daryaft_user
POSTGRES_PASSWORD=change_this_secure_password

# تنظیمات برنامه
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here_min_256_bits
JWT_EXPIRATION=86400000

# تنظیمات پیامک
SMS_API_KEY=your_sms_api_key
SMS_SENDER=your_sender_number

# محدودیت نرخ
RATE_LIMIT_REQUESTS_PER_MINUTE=60
RATE_LIMIT_BURST_CAPACITY=100

# تنظیمات سرور
SERVER_PORT=8080
NGINX_HTTP_PORT=80
NGINX_HTTPS_PORT=443
```

### 3️⃣ قابل اجرا کردن اسکریپت‌ها

```bash
# قابل اجرا کردن اسکریپت‌های استقرار (Linux/macOS)
chmod +x scripts/*.sh

# برای Windows، اسکریپت‌ها با PowerShell/Git Bash اجرا می‌شوند
```

---

## استقرار توسعه

### 🛠️ راه‌اندازی محیط توسعه

```bash
# راه‌اندازی تمام سرویس‌ها با Docker Compose
docker compose up -d

# مشاهده لاگ‌ها
docker compose logs -f
```

### 🌐 دسترسی به برنامه

- **آدرس پایه API**: `http://localhost:8080`
- **رابط Swagger**: `http://localhost:8080/swagger-ui.html`
- **صفحه لندینگ**: `http://localhost/landing.html`
- **داشبورد**: `http://localhost/dashboard.html`

### 🔑 اطلاعات ورود پیش‌فرض

```
نام کاربری: admin@daryaft.com
رمز عبور: Admin123!
```

**⚠️ مهم**: بلافاصله پس از اولین ورود، اطلاعات ورود پیش‌فرض را تغییر دهید!

### 🛑 متوقف کردن محیط توسعه

```bash
# متوقف کردن تمام سرویس‌ها
docker compose down

# متوقف کردن و حذف volumes (⚠️ داده‌ها را حذف می‌کند)
docker compose down -v
```

---

## استقرار تولید

### 1️⃣ پیکربندی محیط

اطمینان حاصل کنید که فایل `.env` شما تنظیمات آماده تولید دارد:

```bash
# تنظیم پروفایل تولید
SPRING_PROFILES_ACTIVE=prod

# استفاده از رمزهای عبور قوی
POSTGRES_PASSWORD=very_strong_random_password_here

# استفاده از کلید JWT امن (حداقل 256 بیت)
JWT_SECRET=generate_random_secure_key_using_openssl_rand_base64_32

# پیکربندی URLهای خارجی
APP_BASE_URL=https://yourdomain.com
```

### 2️⃣ اجرای اسکریپت استقرار

```bash
# Linux/macOS
./scripts/deploy.sh

# Windows (Git Bash)
bash scripts/deploy.sh

# یا استفاده مستقیم از Docker Compose
docker compose -f docker-compose.prod.yml up -d
```

### 3️⃣ تأیید استقرار

```bash
# بررسی کانتینرهای در حال اجرا
docker ps

# بررسی لاگ‌های برنامه
docker compose -f docker-compose.prod.yml logs -f app

# تست نقطه پایانی سلامت
curl http://localhost:8080/actuator/health
```

پاسخ مورد انتظار:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### 4️⃣ پیکربندی پروکسی معکوس

برنامه پشت Nginx اجرا می‌شود. اطمینان حاصل کنید که پیکربندی nginx صحیح است:

```bash
# تست پیکربندی nginx
docker compose exec nginx nginx -t

# بارگذاری مجدد nginx در صورت تغییر پیکربندی
docker compose exec nginx nginx -s reload
```

---

## پشتیبان‌گیری پایگاه داده

### 💾 ایجاد پشتیبان

```bash
# استفاده از اسکریپت پشتیبان (Linux/macOS)
./scripts/backup.sh

# پشتیبان‌گیری دستی با Docker
docker compose exec postgres pg_dump -U daryaft_user daryaft_db > backup_$(date +%Y%m%d_%H%M%S).sql

# پشتیبان فشرده (توصیه می‌شود)
docker compose exec postgres pg_dump -U daryaft_user daryaft_db | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### 📥 بازیابی از پشتیبان

```bash
# استفاده از اسکریپت بازیابی (Linux/macOS)
./scripts/restore.sh backup_20231223_120000.sql

# بازیابی دستی
docker compose exec -T postgres psql -U daryaft_user daryaft_db < backup_20231223_120000.sql

# بازیابی از پشتیبان فشرده
gunzip < backup_20231223_120000.sql.gz | docker compose exec -T postgres psql -U daryaft_user daryaft_db
```

### 📁 محل و نگهداری پشتیبان

- **محل پیش‌فرض**: `./data/backups/`
- **نگهداری پیشنهادی**: 
  - پشتیبان‌های روزانه: نگهداری 7 روز اخیر
  - پشتیبان‌های هفتگی: نگهداری 4 هفته اخیر
  - پشتیبان‌های ماهانه: نگهداری 12 ماه اخیر

```bash
# پشتیبان‌گیری خودکار با cron (افزودن به crontab)
0 2 * * * cd /path/to/daryaft-core && ./scripts/backup.sh >> /var/log/daryaft-backup.log 2>&1
```

---

## نظارت

### 📊 بررسی وضعیت سیستم

```bash
# بررسی وضعیت تمام کانتینرها
docker compose ps

# بررسی استفاده از منابع
docker stats

# استفاده از اسکریپت نظارت (Linux/macOS)
./scripts/monitor.sh
```

### 📝 مشاهده لاگ‌ها

```bash
# مشاهده تمام لاگ‌ها
docker compose logs

# دنبال کردن لاگ‌ها به صورت زنده
docker compose logs -f

# مشاهده لاگ سرویس خاص
docker compose logs -f app
docker compose logs -f postgres
docker compose logs -f nginx

# مشاهده 100 خط آخر
docker compose logs --tail=100 app
```

### 🏥 نقاط پایانی بررسی سلامت

برنامه چندین نقطه پایانی برای بررسی سلامت ارائه می‌دهد:

```bash
# بررسی سلامت اصلی
curl http://localhost:8080/actuator/health

# اطلاعات تفصیلی سلامت
curl http://localhost:8080/actuator/health/detailed

# سلامت پایگاه داده
curl http://localhost:8080/actuator/health/db

# فضای دیسک
curl http://localhost:8080/actuator/health/diskSpace
```

### 📈 نقاط پایانی معیارها

```bash
# معیارهای برنامه
curl http://localhost:8080/actuator/metrics

# معیارهای حافظه JVM
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# معیارهای درخواست‌های HTTP
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## پیکربندی SSL

### 🔐 دریافت گواهی SSL (Let's Encrypt)

```bash
# نصب certbot
sudo apt-get update
sudo apt-get install certbot

# متوقف کردن موقت nginx
docker compose stop nginx

# دریافت گواهی
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# گواهی‌ها در مسیر زیر ذخیره می‌شوند:
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem
# /etc/letsencrypt/live/yourdomain.com/privkey.pem
```

### 📋 کپی کردن گواهی‌ها

```bash
# ایجاد دایرکتوری SSL
mkdir -p nginx/ssl

# کپی کردن گواهی‌ها
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem nginx/ssl/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem nginx/ssl/

# تنظیم مجوزهای مناسب
sudo chmod 644 nginx/ssl/fullchain.pem
sudo chmod 600 nginx/ssl/privkey.pem
```

### ⚙️ به‌روزرسانی پیکربندی Nginx

ویرایش `nginx/nginx.conf` برای فعال‌سازی SSL:

```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # بقیه پیکربندی...
}

server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
```

### 🔄 راه‌اندازی مجدد Nginx

```bash
# تست پیکربندی
docker compose exec nginx nginx -t

# راه‌اندازی مجدد nginx
docker compose restart nginx
```

### 🔁 راه‌اندازی تمدید خودکار

```bash
# افزودن به crontab برای تمدید خودکار
sudo crontab -e

# افزودن این خط (تمدید هر روز در ساعت 2:30 صبح)
30 2 * * * certbot renew --quiet --post-hook "docker compose restart nginx"
```

---

## رفع مشکلات

### ❌ برنامه راه‌اندازی نمی‌شود

**علائم**: کانتینر بلافاصله خارج می‌شود یا راه‌اندازی نمی‌شود

**راه‌حل‌ها**:

```bash
# بررسی لاگ‌های کانتینر
docker compose logs app

# بررسی اینکه آیا پورت قبلاً استفاده می‌شود
netstat -tuln | grep 8080  # Linux/macOS
netstat -ano | findstr :8080  # Windows

# حذف و ایجاد مجدد کانتینرها
docker compose down
docker compose up -d

# بررسی متغیرهای محیطی
docker compose config

# بازسازی کانتینرها
docker compose up -d --build
```

### 🐌 استفاده بالای حافظه

**علائم**: برنامه کند است، کانتینر از حافظه بیش از حد استفاده می‌کند

**راه‌حل‌ها**:

```bash
# بررسی استفاده از حافظه
docker stats

# تنظیم اندازه heap JVM در docker-compose.yml
services:
  app:
    environment:
      - JAVA_OPTS=-Xmx2g -Xms1g

# راه‌اندازی مجدد برنامه
docker compose restart app

# بررسی نشت حافظه در لاگ‌ها
docker compose logs app | grep -i "OutOfMemory"
```

### 🔌 مشکلات اتصال پایگاه داده

**علائم**: خطاهای "Connection refused" یا "Connection timeout"

**راه‌حل‌ها**:

```bash
# بررسی اینکه آیا کانتینر پایگاه داده در حال اجرا است
docker compose ps postgres

# بررسی لاگ‌های پایگاه داده
docker compose logs postgres

# تست اتصال به پایگاه داده
docker compose exec app nc -zv postgres 5432

# تأیید اعتبارنامه‌های پایگاه داده در فایل .env
docker compose exec postgres psql -U daryaft_user -d daryaft_db -c "SELECT version();"

# بررسی اتصال شبکه
docker network inspect regularreception_default

# راه‌اندازی مجدد پایگاه داده
docker compose restart postgres
```

### 🌐 مشکلات Nginx/Proxy

**علائم**: 502 Bad Gateway، مشکلات اتصال

**راه‌حل‌ها**:

```bash
# بررسی لاگ‌های nginx
docker compose logs nginx

# تست پیکربندی nginx
docker compose exec nginx nginx -t

# بررسی اینکه آیا برنامه از nginx قابل دسترسی است
docker compose exec nginx curl http://app:8080/actuator/health

# راه‌اندازی مجدد nginx
docker compose restart nginx
```

### 💾 مشکلات فضای دیسک

**علائم**: برنامه کرش می‌کند، خرابی پشتیبان

**راه‌حل‌ها**:

```bash
# بررسی استفاده از دیسک
df -h

# بررسی استفاده از دیسک Docker
docker system df

# پاکسازی منابع Docker
docker system prune -a --volumes

# حذف فایل‌های لاگ قدیمی
find ./data/logs -name "*.log" -mtime +30 -delete

# پاکسازی کش بیلد Maven
docker compose exec app mvn clean
```

### 🔍 بررسی اتصال

```bash
# تست اتصال خارجی
docker compose exec app curl -I https://google.com

# بررسی رزولوشن DNS
docker compose exec app nslookup google.com

# بررسی ارتباط داخلی کانتینرها
docker compose exec app curl http://postgres:5432
```

---

## چک‌لیست امنیتی

### ✅ امنیت قبل از استقرار

- [ ] **تغییر کلید JWT**
  ```bash
  # تولید کلید JWT امن
  openssl rand -base64 32
  # افزودن به فایل .env
  JWT_SECRET=generated_secret_here
  ```

- [ ] **رمز عبور قوی پایگاه داده**
  ```bash
  # تولید رمز عبور قوی
  openssl rand -base64 24
  # به‌روزرسانی در فایل .env
  POSTGRES_PASSWORD=generated_password_here
  ```

- [ ] **فعال‌سازی HTTPS**
  - [ ] دریافت گواهی SSL (Let's Encrypt یا تجاری)
  - [ ] پیکربندی nginx با SSL
  - [ ] اجبار تغییر مسیر HTTPS
  - [ ] فعال‌سازی هدر HSTS

- [ ] **پیکربندی فایروال**
  ```bash
  # Ubuntu/Debian با ufw
  sudo ufw allow 22/tcp   # SSH
  sudo ufw allow 80/tcp   # HTTP
  sudo ufw allow 443/tcp  # HTTPS
  sudo ufw enable
  
  # بررسی وضعیت فایروال
  sudo ufw status
  ```

- [ ] **فعال‌سازی محدودیت نرخ**
  - [ ] پیکربندی محدودیت‌های نرخ در application.yml
  - [ ] تنظیم محدودیت‌های مناسب در .env
  - [ ] تست عملکرد محدودیت نرخ

- [ ] **پشتیبان‌گیری منظم**
  - [ ] راه‌اندازی cron job پشتیبان‌گیری خودکار
  - [ ] تست روند بازیابی پشتیبان
  - [ ] ذخیره پشتیبان‌ها در مکان امن
  - [ ] پیاده‌سازی سیاست نگهداری پشتیبان

- [ ] **چرخش لاگ**
  ```bash
  # ایجاد پیکربندی logrotate
  sudo nano /etc/logrotate.d/daryaft
  
  # افزودن پیکربندی:
  /path/to/daryaft-core/data/logs/*.log {
      daily
      rotate 14
      compress
      delaycompress
      missingok
      notifempty
      create 0644 root root
  }
  ```

- [ ] **به‌روزرسانی وابستگی‌ها**
  ```bash
  # بررسی به‌روزرسانی‌های وابستگی Maven
  docker compose exec app mvn versions:display-dependency-updates
  
  # به‌روزرسانی تصاویر Docker
  docker compose pull
  ```

### 🔒 اقدامات امنیتی اضافی

- [ ] تغییر اطلاعات ورود پیش‌فرض ادمین
- [ ] پیاده‌سازی 2FA برای حساب‌های ادمین
- [ ] ممیزی امنیتی منظم
- [ ] نظارت بر فعالیت‌های مشکوک
- [ ] به‌روز نگه داشتن برنامه و وابستگی‌ها
- [ ] پیاده‌سازی رمزگذاری پایگاه داده در حالت استراحت
- [ ] استفاده از مدیریت رازها (مثل Docker secrets، HashiCorp Vault)
- [ ] تست نفوذ منظم
- [ ] پیاده‌سازی فایروال برنامه وب (WAF)
- [ ] فعال‌سازی لاگ ممیزی

---

## نگهداری

### 🔄 به‌روزرسانی برنامه

```bash
# دریافت آخرین تغییرات
git pull origin main

# بازسازی و راه‌اندازی مجدد سرویس‌ها
docker compose down
docker compose up -d --build

# یا استفاده از اسکریپت استقرار
./scripts/deploy.sh

# تأیید به‌روزرسانی
docker compose logs -f app
```

### 🧹 پاکسازی تصاویر Docker

```bash
# حذف تصاویر استفاده نشده
docker image prune -a

# حذف کانتینرهای متوقف شده
docker container prune

# حذف volumeهای استفاده نشده (⚠️ با احتیاط با داده‌ها)
docker volume prune

# پاکسازی کامل (⚠️ همه چیز را حذف می‌کند)
docker system prune -a --volumes

# بررسی فضای دیسک ذخیره شده
docker system df
```

### 🗄️ نگهداری پایگاه داده

```bash
# Vacuum پایگاه داده (بازیابی فضا)
docker compose exec postgres psql -U daryaft_user daryaft_db -c "VACUUM VERBOSE;"

# تحلیل پایگاه داده (به‌روزرسانی آمار)
docker compose exec postgres psql -U daryaft_user daryaft_db -c "ANALYZE VERBOSE;"

# بازنمایه‌سازی پایگاه داده
docker compose exec postgres psql -U daryaft_user daryaft_db -c "REINDEX DATABASE daryaft_db;"

# بررسی اندازه پایگاه داده
docker compose exec postgres psql -U daryaft_user daryaft_db -c "SELECT pg_size_pretty(pg_database_size('daryaft_db'));"
```

### 📊 بهینه‌سازی عملکرد

```bash
# بررسی کوئری‌های کند
docker compose exec postgres psql -U daryaft_user daryaft_db -c "SELECT query, mean_exec_time FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;"

# بهینه‌سازی جداول
docker compose exec postgres psql -U daryaft_user daryaft_db -c "VACUUM ANALYZE;"

# بررسی استفاده از ایندکس
docker compose exec postgres psql -U daryaft_user daryaft_db -c "SELECT schemaname, tablename, indexname, idx_scan FROM pg_stat_user_indexes ORDER BY idx_scan;"
```

### 🔍 بررسی منظم سلامت

```bash
# ایجاد اسکریپت نظارت
cat > check_health.sh << 'EOF'
#!/bin/bash
HEALTH_URL="http://localhost:8080/actuator/health"
RESPONSE=$(curl -s $HEALTH_URL | jq -r '.status')

if [ "$RESPONSE" == "UP" ]; then
    echo "✅ برنامه سالم است"
    exit 0
else
    echo "❌ برنامه ناسالم است"
    exit 1
fi
EOF

chmod +x check_health.sh

# افزودن به cron برای بررسی‌های منظم
*/5 * * * * /path/to/check_health.sh >> /var/log/daryaft-health.log 2>&1
```

---

## 📞 Support | پشتیبانی

For issues and questions:
برای مشکلات و سؤالات:

- **GitHub Issues**: [https://github.com/yourusername/daryaft-core/issues](https://github.com/yourusername/daryaft-core/issues)
- **Email**: support@daryaft.com
- **Documentation**: [https://docs.daryaft.com](https://docs.daryaft.com)

---

## 📄 License | مجوز

This project is licensed under the MIT License.
این پروژه تحت مجوز MIT منتشر شده است.

---

**Last Updated | آخرین به‌روزرسانی**: December 23, 2025 | 23 دسامبر 2025
