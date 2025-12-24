#!/bin/bash

# ============================================
# Deployment Script for DaryaftCore Application
# ============================================
# This script automates the deployment process:
# - Pulls latest code from Git
# - Rebuilds Docker images
# - Restarts containers
# - Performs health checks
# ============================================

set -e  # Exit immediately if a command exits with a non-zero status

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${GREEN}ℹ️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to check if .env file exists and load it
load_env() {
    if [ ! -f .env ]; then
        print_error ".env file not found!"
        print_info "Please create a .env file with required environment variables"
        exit 1
    fi
    
    print_info "Loading environment variables from .env file..."
    export $(cat .env | grep -v '^#' | xargs)
    
    # Validate required variables
    required_vars=("POSTGRES_DB" "POSTGRES_USER" "POSTGRES_PASSWORD")
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
