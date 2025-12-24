# Production Deployment Guide

## Prerequisites

1. **Server Requirements:**
   - Linux server (Ubuntu 20.04+ recommended)
   - Docker Engine 20.10+
   - Docker Compose 2.0+
   - At least 4GB RAM, 2 CPU cores
   - 20GB available disk space

2. **SSL Certificate (Optional but Recommended):**
   - Obtain SSL certificate from Let's Encrypt or a certificate authority
   - Place certificate files in `nginx/ssl/` directory

## Deployment Steps

### 1. Prepare Environment

```bash
# Clone the repository
git clone <your-repo-url>
cd RegularReception

# Create production environment file
cp .env.prod.example .env.prod

# Edit the .env.prod file with your actual values
nano .env.prod
```

### 2. Configure SSL (Optional)

If you want to enable HTTPS:

```bash
# Create SSL directory
mkdir -p nginx/ssl

# Copy your SSL certificate files
cp /path/to/certificate.crt nginx/ssl/
cp /path/to/private.key nginx/ssl/

# Edit nginx.conf to uncomment HTTPS server block
nano nginx/nginx.conf
```

### 3. Create Required Directories

```bash
# Create directories for logs and backups
mkdir -p nginx/logs
mkdir -p backup
mkdir -p data

# Set proper permissions
chmod 755 nginx/logs backup data
```

### 4. Build and Start Services

```bash
# Build the application (if not already built)
./mvnw clean package -DskipTests

# Start services using production compose file
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# Check service status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

### 5. Verify Deployment

```bash
# Check if services are healthy
docker-compose -f docker-compose.prod.yml ps

# Test HTTP endpoint
curl http://localhost/api/auth/login

# Test health endpoint
curl http://localhost/health
```

## Post-Deployment

### Monitor Logs

```bash
# Application logs
docker-compose -f docker-compose.prod.yml logs -f app

# Nginx logs
docker-compose -f docker-compose.prod.yml logs -f nginx

# Database logs
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
