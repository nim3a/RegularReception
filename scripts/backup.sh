#!/bin/bash

# ============================================
# Database Backup Script
# ============================================
# This script creates compressed backups of
# the PostgreSQL database running in Docker
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

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# Function to load environment variables
load_env() {
    if [ ! -f .env ]; then
        print_error ".env file not found!"
        exit 1
    fi
    
    print_info "Loading environment variables..."
    export $(cat .env | grep -v '^#' | xargs)
    
    # Validate required variables
    if [ -z "$POSTGRES_DB" ] || [ -z "$POSTGRES_USER" ]; then
        print_error "Required environment variables (POSTGRES_DB, POSTGRES_USER) not set"
        exit 1
    fi
}

# Function to create backup directory
create_backup_dir() {
    BACKUP_DIR="./backups"
    
    if [ ! -d "$BACKUP_DIR" ]; then
        print_info "Creating backup directory..."
        mkdir -p "$BACKUP_DIR"
        print_success "Backup directory created: $BACKUP_DIR"
    fi
}

# Function to generate backup filename
generate_filename() {
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_FILENAME="${POSTGRES_DB}_backup_${TIMESTAMP}.sql.gz"
    BACKUP_PATH="${BACKUP_DIR}/${BACKUP_FILENAME}"
    
    print_info "Backup will be saved as: $BACKUP_FILENAME"
}

# Function to perform database backup
perform_backup() {
    print_info "Starting database backup..."
    
    # Check if database container is running
    if ! docker-compose -f docker-compose.prod.yml ps postgres | grep -q "Up"; then
        print_error "PostgreSQL container is not running!"
        exit 1
    fi
    
    # Execute pg_dump inside Docker container and compress with gzip
    if docker-compose -f docker-compose.prod.yml exec -T postgres pg_dump \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        --clean \
        --if-exists \
        --create \
        | gzip > "$BACKUP_PATH"; then
        
        # Get backup file size
        BACKUP_SIZE=$(du -h "$BACKUP_PATH" | cut -f1)
        
        print_success "Database backup completed successfully! 🎉"
        echo ""
        echo "Backup Details:"
        echo "  📁 File: $BACKUP_FILENAME"
        echo "  📊 Size: $BACKUP_SIZE"
        echo "  📂 Path: $(realpath "$BACKUP_PATH")"
    else
        print_error "Database backup failed!"
        # Remove partial backup file if it exists
        [ -f "$BACKUP_PATH" ] && rm "$BACKUP_PATH"
        exit 1
    fi
}

# Function to cleanup old backups
cleanup_old_backups() {
    print_info "Cleaning up old backups (older than 30 days)..."
    
    # Find and delete backups older than 30 days
    DELETED_COUNT=$(find "$BACKUP_DIR" -name "*.sql.gz" -type f -mtime +30 -delete -print | wc -l)
    
    if [ "$DELETED_COUNT" -gt 0 ]; then
        print_success "Deleted $DELETED_COUNT old backup(s)"
    else
        print_info "No old backups to delete"
    fi
}

# Main backup process
main() {
    echo "============================================"
    echo "💾 Database Backup Process"
    echo "============================================"
    echo ""
    
    # Change to script directory's parent (project root)
    cd "$(dirname "$0")/.."
    
    load_env
    create_backup_dir
    generate_filename
    perform_backup
    cleanup_old_backups
    
    echo ""
    echo "============================================"
    echo "✅ Backup process completed!"
    echo "============================================"
}

# Trap errors
trap 'print_error "An error occurred during backup. Exiting..."; exit 1' ERR

# Execute main function
main
