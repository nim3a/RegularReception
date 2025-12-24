#!/bin/bash

# ============================================
# System Monitoring Script
# ============================================
# This script monitors the health and status
# of the application, database, and system
# ============================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored headers
print_header() {
    echo ""
    echo -e "${CYAN}============================================${NC}"
    echo -e "${CYAN}$1${NC}"
    echo -e "${CYAN}============================================${NC}"
}

print_section() {
    echo ""
    echo -e "${BLUE}📊 $1${NC}"
    echo "--------------------------------------------"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${GREEN}ℹ️  $1${NC}"
}

# Function to load environment variables
load_env() {
    if [ -f .env ]; then
        export $(cat .env | grep -v '^#' | xargs) 2>/dev/null
    fi
}

# Function to check container status
check_container_status() {
    print_section "Container Status"
    
    if command -v docker-compose &> /dev/null; then
        docker-compose -f docker-compose.prod.yml ps
        echo ""
        
        # Count running containers
        RUNNING_CONTAINERS=$(docker-compose -f docker-compose.prod.yml ps --services --filter "status=running" | wc -l)
        TOTAL_CONTAINERS=$(docker-compose -f docker-compose.prod.yml ps --services | wc -l)
        
        if [ "$RUNNING_CONTAINERS" -eq "$TOTAL_CONTAINERS" ]; then
            print_success "All containers are running ($RUNNING_CONTAINERS/$TOTAL_CONTAINERS)"
        else
            print_warning "Some containers are not running ($RUNNING_CONTAINERS/$TOTAL_CONTAINERS)"
        fi
    else
        print_error "docker-compose not found"
    fi
}

# Function to check application health
check_application_health() {
    print_section "Application Health"
    
    APP_PORT=${SERVER_PORT:-8080}
    HEALTH_URL="http://localhost:${APP_PORT}/actuator/health"
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        print_warning "curl not found - skipping health check"
        return
    fi
    
    # Attempt to get health status
    RESPONSE=$(curl -s -w "\n%{http_code}" "$HEALTH_URL" 2>/dev/null || echo "000")
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        print_success "Application is healthy (HTTP $HTTP_CODE)"
        echo "  URL: $HEALTH_URL"
        
        # Try to parse with jq if available
        if command -v jq &> /dev/null && [ -n "$BODY" ]; then
            echo ""
            echo "Health Details:"
            echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
        fi
    else
        print_error "Application health check failed (HTTP $HTTP_CODE)"
        echo "  URL: $HEALTH_URL"
    fi
}

# Function to check database status
check_database_status() {
    print_section "Database Status"
    
    # Check if database container is running
    if docker-compose -f docker-compose.prod.yml ps postgres | grep -q "Up"; then
        print_success "PostgreSQL container is running"
        
        # Check database connectivity using pg_isready
        if docker-compose -f docker-compose.prod.yml exec -T postgres pg_isready -U "${POSTGRES_USER:-postgres}" > /dev/null 2>&1; then
            print_success "Database is accepting connections"
        else
            print_error "Database is not accepting connections"
        fi
        
        # Get database size if POSTGRES_DB is set
        if [ -n "$POSTGRES_DB" ]; then
            DB_SIZE=$(docker-compose -f docker-compose.prod.yml exec -T postgres psql \
                -U "${POSTGRES_USER:-postgres}" \
                -d "$POSTGRES_DB" \
                -t -c "SELECT pg_size_pretty(pg_database_size('$POSTGRES_DB'));" 2>/dev/null | xargs)
            
            if [ -n "$DB_SIZE" ]; then
                echo "  Database: $POSTGRES_DB"
                echo "  Size: $DB_SIZE"
            fi
        fi
    else
        print_error "PostgreSQL container is not running"
    fi
}

# Function to check disk usage
check_disk_usage() {
    print_section "Disk Usage"
    
    df -h | grep -E "Filesystem|/$|/home" || df -h
    
    # Warning if disk usage is above 80%
    DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')
    
    if [ "$DISK_USAGE" -gt 80 ]; then
        print_warning "Disk usage is above 80%: ${DISK_USAGE}%"
    fi
}

# Function to check memory usage
check_memory_usage() {
    print_section "Memory Usage"
    
    if command -v free &> /dev/null; then
        free -h
        
        # Calculate memory usage percentage
        MEMORY_USAGE=$(free | awk 'NR==2 {printf "%.0f", $3*100/$2}')
        
        if [ "$MEMORY_USAGE" -gt 80 ]; then
            print_warning "Memory usage is above 80%: ${MEMORY_USAGE}%"
        fi
    else
        print_warning "free command not available"
    fi
}

# Function to show recent application logs
show_recent_logs() {
    print_section "Recent Application Logs (Last 20 lines)"
    
    if docker-compose -f docker-compose.prod.yml ps app | grep -q "Up"; then
        docker-compose -f docker-compose.prod.yml logs --tail=20 app
    else
        print_warning "Application container is not running"
    fi
}

# Function to show active database connections
show_database_connections() {
    print_section "Active Database Connections"
    
    if docker-compose -f docker-compose.prod.yml ps postgres | grep -q "Up"; then
        CONNECTION_COUNT=$(docker-compose -f docker-compose.prod.yml exec -T postgres psql \
            -U "${POSTGRES_USER:-postgres}" \
            -d "${POSTGRES_DB:-postgres}" \
            -t -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active';" 2>/dev/null | xargs)
        
        if [ -n "$CONNECTION_COUNT" ]; then
            echo "  Active connections: $CONNECTION_COUNT"
            
            # Show connection details
            echo ""
            echo "Connection Details:"
            docker-compose -f docker-compose.prod.yml exec -T postgres psql \
                -U "${POSTGRES_USER:-postgres}" \
                -d "${POSTGRES_DB:-postgres}" \
                -c "SELECT datname, usename, application_name, state, query_start 
                    FROM pg_stat_activity 
                    WHERE state = 'active' 
                    ORDER BY query_start DESC 
                    LIMIT 10;" 2>/dev/null || print_warning "Could not fetch connection details"
        else
            print_warning "Could not retrieve connection count"
        fi
    else
        print_error "PostgreSQL container is not running"
    fi
}

# Main monitoring process
main() {
    clear
    print_header "🔍 System Monitoring Dashboard"
    echo ""
    echo "Generated at: $(date '+%Y-%m-%d %H:%M:%S')"
    
    # Change to script directory's parent (project root)
    cd "$(dirname "$0")/.."
    
    load_env
    
    check_container_status
    check_application_health
    check_database_status
    check_disk_usage
    check_memory_usage
    show_database_connections
    show_recent_logs
    
    echo ""
    print_header "✅ Monitoring Complete"
    echo ""
}

# Execute main function
main
