# RegularReception Production Deployment Guide

## 📋 جدول محتویات

1. [پیش‌نیازها](#پیش-نیازها)
2. [آماده‌سازی سرور](#آماده-سازی-سرور)
3. [نصب Dependencies](#نصب-dependencies)
4. [تنظیمات Security](#تنظیمات-security)
5. [راه‌اندازی SSL با Let's Encrypt](#راه-اندازی-ssl)
6. [Deploy اپلیکیشن](#deploy-اپلیکیشن)
7. [Monitoring و Logging](#monitoring-و-logging)
8. [Backup و Recovery](#backup-و-recovery)
9. [Troubleshooting](#troubleshooting)
10. [Best Practices](#best-practices)

---

## 🔧 پیش‌نیازها

### سخت‌افزار مورد نیاز

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 2 Cores | 4+ Cores |
| RAM | 4 GB | 8+ GB |
| Storage | 20 GB SSD | 50+ GB SSD |
| Network | 100 Mbps | 1 Gbps |

### نرم‌افزار مورد نیاز

- **OS**: Ubuntu 20.04/22.04 LTS یا CentOS 8+
- **Docker**: 24.0+
- **Docker Compose**: 2.20+
- **Java**: 21 (برای build محلی)
- **Maven**: 3.8+ (برای build محلی)
- **Git**: 2.30+
- **Curl**: 7.68+

---

## 🖥️ آماده‌سازی سرور

### 1. به‌روزرسانی سیستم

```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl wget git nano ufw

# CentOS/RHEL
sudo yum update -y
sudo yum install -y curl wget git nano firewalld
```

### 2. ایجاد کاربر جدید (اختیاری اما پیشنهادی)

```bash
# ایجاد کاربر deploy
sudo adduser deploy
sudo usermod -aG sudo deploy
sudo usermod -aG docker deploy

# سوئیچ به کاربر جدید
su - deploy
```

### 3. تنظیم Timezone

```bash
# تنظیم timezone به تهران
sudo timedatectl set-timezone Asia/Tehran

# بررسی timezone
timedatectl
```

---

## 📦 نصب Dependencies

### 1. نصب Docker

```bash
# حذف نسخه‌های قدیمی Docker
sudo apt remove docker docker-engine docker.io containerd runc

# نصب dependencies
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release

# افزودن Docker GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
    sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# افزودن repository
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# نصب Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io \
    docker-buildx-plugin docker-compose-plugin

# بررسی نصب
docker --version
docker compose version

# اجرای Docker بدون sudo
sudo usermod -aG docker $USER
newgrp docker

# تست Docker
docker run hello-world
```

### 2. نصب Java 21 (برای Build)

```bash
# نصب OpenJDK 21
sudo apt install -y openjdk-21-jdk

# بررسی نصب
java -version
```

### 3. نصب Maven

```bash
# دانلود Maven
cd /opt
sudo wget https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
sudo tar xzf apache-maven-3.9.6-bin.tar.gz
sudo ln -s apache-maven-3.9.6 maven

# تنظیم متغیرهای محیطی
echo 'export M2_HOME=/opt/maven' | sudo tee -a /etc/profile.d/maven.sh
echo 'export PATH=${M2_HOME}/bin:${PATH}' | sudo tee -a /etc/profile.d/maven.sh
sudo chmod +x /etc/profile.d/maven.sh
source /etc/profile.d/maven.sh

# بررسی نصب
mvn -version
```

---

## 🔒 تنظیمات Security

### 1. تنظیم Firewall

```bash
# فعال‌سازی UFW
sudo ufw enable

# اجازه SSH
sudo ufw allow 22/tcp

# اجازه HTTP و HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# بررسی وضعیت
sudo ufw status verbose
```

### 2. تنظیمات SSH

```bash
# ویرایش تنظیمات SSH
sudo nano /etc/ssh/sshd_config

# تغییرات پیشنهادی:
# PermitRootLogin no
# PasswordAuthentication no  # بعد از تنظیم SSH key
# PubkeyAuthentication yes
# Port 22  # یا پورت دلخواه

# راه‌اندازی مجدد SSH
sudo systemctl restart sshd
```

### 3. نصب Fail2Ban

```bash
# نصب Fail2Ban
sudo apt install -y fail2ban

# کپی تنظیمات پیش‌فرض
sudo cp /etc/fail2ban/jail.conf /etc/fail2ban/jail.local

# ویرایش تنظیمات
sudo nano /etc/fail2ban/jail.local

# فعال‌سازی و راه‌اندازی
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

---

## 🔐 راه‌اندازی SSL

### روش 1: استفاده از Let's Encrypt (رایگان)

```bash
# 1. ابتدا Nginx را بدون SSL راه‌اندازی کنید
cd /path/to/RegularReception
docker compose -f docker-compose.prod.yml up -d nginx

# 2. دریافت گواهی SSL
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email admin@yourdomain.com \
    --agree-tos \
    --no-eff-email \
    -d yourdomain.com \
    -d www.yourdomain.com

# 3. به‌روزرسانی nginx.conf با domain خود
nano nginx/nginx.conf
# جایگزین کردن 'yourdomain.com' با دامنه واقعی

# 4. راه‌اندازی مجدد Nginx
docker compose -f docker-compose.prod.yml restart nginx

# 5. تست SSL
curl https://yourdomain.com/health
```

### تنظیم Auto-Renewal برای Let's Encrypt

```bash
# certbot container به صورت خودکار هر 12 ساعت یکبار گواهی را تمدید می‌کند
# بررسی logs:
docker compose -f docker-compose.prod.yml logs certbot

# تست تمدید دستی:
docker compose -f docker-compose.prod.yml run --rm certbot renew --dry-run
```

---

## 🚀 Deploy اپلیکیشن

### 1. Clone پروژه

```bash
# Clone repository
git clone https://github.com/yourusername/RegularReception.git
cd RegularReception
```

### 2. تنظیم Environment Variables

```bash
# کپی فایل نمونه
cp .env.prod.example .env.prod

# ویرایش فایل با مقادیر واقعی
nano .env.prod
```

#### مقادیر مهم که باید تغییر کنید:

```bash
# Database
POSTGRES_PASSWORD=your_strong_password_here

# Redis
REDIS_PASSWORD=your_redis_password_here

# JWT Secret (generate: openssl rand -base64 64)
JWT_SECRET=your_jwt_secret_base64_here

# SMS (MeliPayamak)
SMS_USERNAME=your_melipayamak_username
SMS_PASSWORD=your_melipayamak_password
SMS_FROM=your_sms_number

# Payment Gateway
PAYMENT_GATEWAY_URL=https://your-payment-gateway.com
PAYMENT_MERCHANT_ID=your_merchant_id
PAYMENT_API_KEY=your_payment_api_key

# Domain
DOMAIN_NAME=yourdomain.com
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 3. ایجاد دایرکتوری‌های مورد نیاز

```bash
# ایجاد دایرکتوری‌ها
sudo mkdir -p /var/lib/regularreception/postgres
sudo mkdir -p /var/lib/regularreception/redis
sudo mkdir -p backups
sudo mkdir -p uploads

# تنظیم permissions
sudo chown -R $USER:$USER /var/lib/regularreception
chmod 755 backups uploads
```

### 4. Build اپلیکیشن

```bash
# Build با Maven
./mvnw clean package -DskipTests

# یا با skip tests:
./mvnw clean package -Dmaven.test.skip=true
```

### 5. Deploy با اسکریپت خودکار

```bash
# اجازه اجرا به اسکریپت
chmod +x scripts/deploy.sh

# اجرای deploy
./scripts/deploy.sh
```

یا به صورت دستی:

```bash
# Build و Start containers
docker compose -f docker-compose.prod.yml up -d --build

# بررسی وضعیت
docker compose -f docker-compose.prod.yml ps

# مشاهده logs
docker compose -f docker-compose.prod.yml logs -f
```

### 6. بررسی سلامت اپلیکیشن

```bash
# Health Check
curl http://localhost:8080/actuator/health

# یا از طریق Nginx
curl https://yourdomain.com/actuator/health

# بررسی متریک‌ها
curl http://localhost:8080/actuator/metrics

# بررسی Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

---

## 📊 Monitoring و Logging

### 1. مشاهده Logs

```bash
# Logs تمام سرویس‌ها
docker compose -f docker-compose.prod.yml logs -f

# Logs فقط اپلیکیشن
docker compose -f docker-compose.prod.yml logs -f app

# Logs فقط Nginx
docker compose -f docker-compose.prod.yml logs -f nginx

# Logs فقط PostgreSQL
docker compose -f docker-compose.prod.yml logs -f postgres

# Logs با تعداد خط مشخص
docker compose -f docker-compose.prod.yml logs --tail=100 -f app
```

### 2. استفاده از اسکریپت Monitoring

```bash
# اجازه اجرا به اسکریپت
chmod +x scripts/monitor.sh

# اجرای monitoring
./scripts/monitor.sh

# Monitoring مداوم (هر 5 ثانیه)
watch -n 5 './scripts/monitor.sh'
```

### 3. نصب Prometheus و Grafana (اختیاری)

```yaml
# افزودن به docker-compose.prod.yml:

  prometheus:
    image: prom/prometheus:latest
    container_name: regularreception-prometheus
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    ports:
      - "127.0.0.1:9090:9090"
    networks:
      - backend-network

  grafana:
    image: grafana/grafana:latest
    container_name: regularreception-grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    ports:
      - "127.0.0.1:3000:3000"
    networks:
      - backend-network
```

---

## 💾 Backup و Recovery

### 1. Backup خودکار

```bash
# اجازه اجرا به اسکریپت
chmod +x scripts/backup.sh

# ایجاد backup
./scripts/backup.sh

# لیست backup‌ها
./scripts/backup.sh --list

# ایجاد checkpoint backup
./scripts/backup.sh --checkpoint
```

### 2. تنظیم Cron Job برای Backup خودکار

```bash
# ویرایش crontab
crontab -e

# افزودن این خط برای backup روزانه ساعت 2 بامداد:
0 2 * * * cd /path/to/RegularReception && ./scripts/backup.sh >> /var/log/regularreception-backup.log 2>&1

# Backup هفتگی (یکشنبه‌ها ساعت 3 بامداد):
0 3 * * 0 cd /path/to/RegularReception && ./scripts/backup.sh --checkpoint >> /var/log/regularreception-backup.log 2>&1
```

### 3. Restore از Backup

```bash
# اجازه اجرا به اسکریپت
chmod +x scripts/restore.sh

# لیست backup‌های موجود
./scripts/restore.sh --list

# Restore از backup (interactive)
./scripts/restore.sh

# Restore از فایل مشخص
./scripts/restore.sh backups/backup_20231223_120000.sql.gz
```

---

## 🔍 Troubleshooting

### مشکلات رایج و راه‌حل‌ها

#### 1. Container راه‌اندازی نمی‌شود

```bash
# بررسی logs
docker compose -f docker-compose.prod.yml logs app

# بررسی وضعیت
docker compose -f docker-compose.prod.yml ps

# راه‌اندازی مجدد
docker compose -f docker-compose.prod.yml restart app

# Build مجدد
docker compose -f docker-compose.prod.yml up -d --build --force-recreate app
```

#### 2. خطای Connection به Database

```bash
# بررسی PostgreSQL
docker compose -f docker-compose.prod.yml logs postgres

# بررسی متغیرهای محیطی
docker compose -f docker-compose.prod.yml exec app env | grep POSTGRES

# تست اتصال
docker compose -f docker-compose.prod.yml exec postgres psql -U admin -d regularreception
```

#### 3. خطای 502 Bad Gateway در Nginx

```bash
# بررسی Nginx logs
docker compose -f docker-compose.prod.yml logs nginx

# بررسی اتصال به backend
docker compose -f docker-compose.prod.yml exec nginx ping app

# راه‌اندازی مجدد Nginx
docker compose -f docker-compose.prod.yml restart nginx
```

#### 4. مشکل SSL Certificate

```bash
# بررسی certbot logs
docker compose -f docker-compose.prod.yml logs certbot

# تست تمدید دستی
docker compose -f docker-compose.prod.yml run --rm certbot renew --dry-run

# دریافت مجدد گواهی
docker compose -f docker-compose.prod.yml run --rm certbot certonly --force-renew \
    --webroot --webroot-path=/var/www/certbot \
    -d yourdomain.com
```

#### 5. Memory/CPU بالا

```bash
# بررسی resource usage
docker stats

# بررسی logs برای memory leak
docker compose -f docker-compose.prod.yml logs app | grep -i "memory\|heap"

# تنظیم JAVA_OPTS در docker-compose.prod.yml
```

---

## 📚 Best Practices

### 1. Security

- ✅ استفاده از HTTPS در production
- ✅ تغییر تمام پسوردهای پیش‌فرض
- ✅ استفاده از JWT secrets قوی
- ✅ فعال‌سازی Firewall
- ✅ به‌روزرسانی منظم سیستم و Docker images
- ✅ محدود کردن دسترسی به Actuator endpoints
- ✅ استفاده از RBAC برای کاربران

### 2. Performance

- ✅ استفاده از Redis برای caching
- ✅ تنظیم connection pool sizes مناسب
- ✅ فعال‌سازی Gzip compression
- ✅ استفاده از CDN برای static assets
- ✅ Monitoring منابع سیستم
- ✅ تنظیم JVM heap size مناسب

### 3. Reliability

- ✅ Backup روزانه خودکار
- ✅ Health checks برای تمام سرویس‌ها
- ✅ Logging مناسب
- ✅ استفاده از restart policies
- ✅ تست deployment در staging قبل از production
- ✅ داشتن rollback plan

### 4. Monitoring

- ✅ استفاده از Prometheus و Grafana
- ✅ تنظیم alerts برای مشکلات
- ✅ Monitoring disk space
- ✅ بررسی منظم logs
- ✅ Monitoring response times

---

## 🔧 Commands مفید

### Docker

```bash
# مشاهده resource usage
docker stats

# پاک‌سازی resources غیرضروری
docker system prune -a --volumes

# بررسی disk usage
docker system df

# خروج از container
docker compose -f docker-compose.prod.yml exec app bash

# کپی فایل از/به container
docker cp file.txt regularreception-app-prod:/tmp/
docker cp regularreception-app-prod:/tmp/file.txt ./
```

### Database

```bash
# اتصال به PostgreSQL
docker compose -f docker-compose.prod.yml exec postgres psql -U admin -d regularreception

# لیست دیتابیس‌ها
docker compose -f docker-compose.prod.yml exec postgres psql -U admin -l

# Backup دستی
docker compose -f docker-compose.prod.yml exec -T postgres pg_dump \
    -U admin regularreception | gzip > manual_backup_$(date +%Y%m%d).sql.gz

# Vacuum database
docker compose -f docker-compose.prod.yml exec postgres psql -U admin -d regularreception -c "VACUUM ANALYZE;"
```

### Application

```bash
# بررسی version
curl http://localhost:8080/actuator/info

# Restart application
docker compose -f docker-compose.prod.yml restart app

# مشاهده environment variables
docker compose -f docker-compose.prod.yml exec app env

# بررسی Java version
docker compose -f docker-compose.prod.yml exec app java -version
```

---

## 📞 Support و Documentation

### منابع اضافی

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)

### تماس با پشتیبانی

- Email: support@yourdomain.com
- GitHub Issues: [project repository]/issues

---

**نسخه:** 1.0.0  
**آخرین به‌روزرسانی:** 2024-12-24  
**مخصوص:** RegularReception Production Deployment
docker-compose -f docker-compose.prod.yml logs -f postgres
```

### Backup Database

```bash
# Manual backup
docker exec daryaft-postgres-prod pg_dump -U daryaftuser daryaftdb > ./backup/backup_$(date +%Y%m%d_%H%M%S).sql

# Restore from backup
docker exec -i daryaft-postgres-prod psql -U daryaftuser daryaftdb < ./backup/backup_20231223_120000.sql
```

### Update Application

```bash
# Pull latest changes
git pull origin main

# Rebuild application
./mvnw clean package -DskipTests

# Recreate app service
docker-compose -f docker-compose.prod.yml up -d --build app

# Check logs
docker-compose -f docker-compose.prod.yml logs -f app
```

### Scale Services

```bash
# Scale app service (for load balancing)
docker-compose -f docker-compose.prod.yml up -d --scale app=3
```

## Security Hardening

### 1. Firewall Configuration

```bash
# Allow only necessary ports
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable
```

### 2. SSL/TLS Best Practices

- Use strong SSL protocols (TLS 1.2+)
- Regularly update certificates
- Enable HSTS (Strict-Transport-Security)
- Consider using Let's Encrypt for free certificates

### 3. Database Security

- Use strong passwords
- Restrict database port access (only from app service)
- Regular backups
- Enable SSL connections for PostgreSQL

### 4. Application Security

- Keep JWT secret secure and complex
- Rotate secrets regularly
- Monitor rate limiting logs
- Review security headers in Nginx

## Monitoring

### Resource Usage

```bash
# Check container resource usage
docker stats

# Check disk usage
docker system df
```

### Health Checks

```bash
# Check application health
curl http://localhost/actuator/health

# Check Nginx health
curl http://localhost/health
```

## Troubleshooting

### Service Won't Start

```bash
# Check logs for errors
docker-compose -f docker-compose.prod.yml logs

# Check service status
docker-compose -f docker-compose.prod.yml ps

# Restart specific service
docker-compose -f docker-compose.prod.yml restart app
```

### Connection Issues

```bash
# Check network connectivity
docker network inspect regularreception_daryaft-network-prod

# Test internal connectivity
docker exec daryaft-app-prod curl -f http://postgres:5432
```

### Performance Issues

```bash
# Check resource usage
docker stats

# View application metrics
curl http://localhost/actuator/metrics

# Check database connections
docker exec daryaft-postgres-prod psql -U daryaftuser -d daryaftdb -c "SELECT * FROM pg_stat_activity;"
```

## Cleanup

```bash
# Stop all services
docker-compose -f docker-compose.prod.yml down

# Remove volumes (WARNING: This deletes data!)
docker-compose -f docker-compose.prod.yml down -v

# Clean up unused Docker resources
docker system prune -a
```

## Production Checklist

- [ ] Environment variables configured in .env.prod
- [ ] Strong passwords set for database and JWT
- [ ] SSL certificates installed (if using HTTPS)
- [ ] Firewall configured
- [ ] Backup directory created and writable
- [ ] Application built successfully
- [ ] All services started and healthy
- [ ] Health endpoints responding
- [ ] Rate limiting tested
- [ ] Monitoring configured
- [ ] Regular backup schedule established

## Support

For issues or questions:
- Check logs: `docker-compose -f docker-compose.prod.yml logs -f`
- Review documentation in the repository
- Check GitHub issues
