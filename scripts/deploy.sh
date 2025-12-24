#!/bin/bash

################################################################################
# RegularReception Production Deployment Script
# Description: Automated deployment script for production environment
# Author: DevOps Team
# Version: 1.0.0
################################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="regularreception"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${PROJECT_DIR}/.env.prod"
BACKUP_DIR="${PROJECT_DIR}/backups"
LOG_FILE="${PROJECT_DIR}/deployment.log"

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$LOG_FILE"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$LOG_FILE"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "$LOG_FILE"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$LOG_FILE"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    local missing_deps=()
    
    if ! command_exists docker; then
        missing_deps+=("docker")
    fi
    
    if ! command_exists docker-compose; then
        missing_deps+=("docker-compose")
    fi
    
    if ! command_exists mvn; then
        missing_deps+=("maven")
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            print_error "Required environment variable $var is not set in .env file"
            exit 1
        fi
    done
    
    print_success "Environment variables loaded successfully"
}

# Function to pull latest code
pull_code() {
    print_info "Pulling latest code from Git repository..."
    
    if ! git pull origin main; then
        print_error "Failed to pull latest code from Git"
        exit 1
    fi
    
    print_success "Code updated successfully"
}

# Function to build Docker images
build_images() {
    print_info "Building Docker images with --no-cache flag..."
    
    if ! docker-compose -f docker-compose.prod.yml build --no-cache; then
        print_error "Failed to build Docker images"
        exit 1
    fi
    
    print_success "Docker images built successfully"
}

# Function to stop existing containers
stop_containers() {
    print_info "Stopping existing containers gracefully..."
    
    if docker-compose -f docker-compose.prod.yml ps -q | grep -q .; then
        if ! docker-compose -f docker-compose.prod.yml down --timeout 30; then
            print_error "Failed to stop containers"
            exit 1
        fi
        print_success "Containers stopped successfully"
    else
        print_warning "No running containers found"
    fi
}

# Function to start containers
start_containers() {
    print_info "Starting new containers in detached mode..."
    
    if ! docker-compose -f docker-compose.prod.yml up -d; then
        print_error "Failed to start containers"
        exit 1
    fi
    
    print_success "Containers started successfully"
}

# Function to wait for application startup
wait_for_startup() {
    print_info "Waiting 30 seconds for application startup..."
    sleep 30
}

# Function to perform health check
health_check() {
    print_info "Performing health check..."
    
    # Try to get the application port from environment or use default
    APP_PORT=${SERVER_PORT:-8080}
    HEALTH_URL="http://localhost:${APP_PORT}/actuator/health"
    
    # Retry health check up to 5 times
    MAX_RETRIES=5
    RETRY_COUNT=0
    
    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if curl -f -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" | grep -q "200"; then
            print_success "Application is healthy! 🎉"
            echo ""
            echo "Deployment completed successfully! 🚀"
            echo "Application is running at: http://localhost:${APP_PORT}"
            return 0
        fi
        
        RETRY_COUNT=$((RETRY_COUNT + 1))
        print_warning "Health check attempt $RETRY_COUNT/$MAX_RETRIES failed. Retrying in 10 seconds..."
        sleep 10
    done
    
    print_error "Health check failed after $MAX_RETRIES attempts"
    print_error "Deployment may have issues. Check logs with: docker-compose -f docker-compose.prod.yml logs"
    exit 1
}

# Main deployment process
main() {
    echo "============================================"
    echo "🚀 Starting Deployment Process"
    echo "============================================"
    echo ""
    
    # Change to script directory's parent (project root)
    cd "$(dirname "$0")/.."
    
    load_env
    pull_code
    build_images
    stop_containers
    start_containers
    wait_for_startup
    health_check
}

# Execute main function
main
