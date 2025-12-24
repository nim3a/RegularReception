#!/bin/bash

################################################################################
# RegularReception Database Backup Script
# Description: Creates automated backups of PostgreSQL database
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
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
ENV_FILE="${PROJECT_DIR}/.env.prod"
BACKUP_DIR="${PROJECT_DIR}/backups"
LOG_FILE="${PROJECT_DIR}/backup.log"

# Backup settings
BACKUP_RETENTION_DAYS=30
DATE_FORMAT=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="backup_${DATE_FORMAT}.sql.gz"

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

# Function to check if docker-compose is running
check_docker_compose() {
    print_info "Checking if PostgreSQL container is running..."
    
    cd "$PROJECT_DIR"
    
    if ! docker-compose -f docker-compose.prod.yml ps postgres | grep -q "Up"; then
        print_error "PostgreSQL container is not running."
        print_error "Please start the containers first: docker-compose -f docker-compose.prod.yml up -d"
        exit 1
    fi
    
    print_success "PostgreSQL container is running."
}

# Function to load environment variables
load_env() {
    print_info "Loading environment variables..."
    
    if [ ! -f "$ENV_FILE" ]; then
        print_error "Environment file not found: $ENV_FILE"
        exit 1
    fi
    
    set -a
    source "$ENV_FILE"
    set +a
    
    # Set default values if not defined
    POSTGRES_DB=${POSTGRES_DB:-regularreception}
    POSTGRES_USER=${POSTGRES_USER:-admin}
    
    if [ -z "${POSTGRES_PASSWORD:-}" ]; then
        print_error "POSTGRES_PASSWORD is not set in .env.prod"
        exit 1
    fi
    
    print_success "Environment variables loaded."
}

# Function to create backup directory
create_backup_dir() {
    print_info "Creating backup directory..."
    
    mkdir -p "$BACKUP_DIR"
    
    if [ $? -eq 0 ]; then
        print_success "Backup directory created: $BACKUP_DIR"
    else
        print_error "Failed to create backup directory."
        exit 1
    fi
}

# Function to create database backup
create_backup() {
    print_info "Creating database backup: $BACKUP_FILE"
    
    cd "$PROJECT_DIR"
    
    # Create backup using pg_dump inside the container
    docker-compose -f docker-compose.prod.yml exec -T postgres pg_dump \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        --no-owner \
        --no-acl \
        --clean \
        --if-exists \
        | gzip > "${BACKUP_DIR}/${BACKUP_FILE}"
    
    if [ $? -eq 0 ] && [ -f "${BACKUP_DIR}/${BACKUP_FILE}" ]; then
        local backup_size=$(du -h "${BACKUP_DIR}/${BACKUP_FILE}" | cut -f1)
        print_success "Backup created successfully: $BACKUP_FILE (Size: $backup_size)"
    else
        print_error "Failed to create backup."
        exit 1
    fi
}

# Function to verify backup
verify_backup() {
    print_info "Verifying backup integrity..."
    
    # Check if backup file exists and is not empty
    if [ ! -s "${BACKUP_DIR}/${BACKUP_FILE}" ]; then
        print_error "Backup file is empty or does not exist."
        exit 1
    fi
    
    # Test gzip integrity
    if gzip -t "${BACKUP_DIR}/${BACKUP_FILE}" 2>/dev/null; then
        print_success "Backup integrity verified."
    else
        print_error "Backup file is corrupted."
        exit 1
    fi
}

# Function to create metadata file
create_metadata() {
    print_info "Creating backup metadata..."
    
    local metadata_file="${BACKUP_DIR}/${BACKUP_FILE}.meta"
    
    cat > "$metadata_file" <<EOF
Backup Metadata
===============
Database: $POSTGRES_DB
User: $POSTGRES_USER
Date: $(date)
Backup File: $BACKUP_FILE
Size: $(du -h "${BACKUP_DIR}/${BACKUP_FILE}" | cut -f1)
PostgreSQL Version: $(docker-compose -f docker-compose.prod.yml exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -t -c "SELECT version();" | head -n 1 | xargs)
EOF
    
    if [ -f "$metadata_file" ]; then
        print_success "Metadata file created: ${BACKUP_FILE}.meta"
    fi
}

# Function to cleanup old backups
cleanup_old_backups() {
    print_info "Cleaning up old backups (older than $BACKUP_RETENTION_DAYS days)..."
    
    local deleted_count=0
    
    # Find and delete old backup files
    while IFS= read -r old_backup; do
        rm -f "$old_backup"
        rm -f "${old_backup}.meta"
        deleted_count=$((deleted_count + 1))
        print_info "Deleted old backup: $(basename "$old_backup")"
    done < <(find "$BACKUP_DIR" -name "backup_*.sql.gz" -type f -mtime +${BACKUP_RETENTION_DAYS})
    
    if [ $deleted_count -gt 0 ]; then
        print_success "Deleted $deleted_count old backup(s)."
    else
        print_info "No old backups to delete."
    fi
}

# Function to list all backups
list_backups() {
    print_info "Available backups:"
    echo "========================================"
    
    if [ ! -d "$BACKUP_DIR" ] || [ -z "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]; then
        print_warning "No backups found."
        return
    fi
    
    # List all backup files with size and date
    find "$BACKUP_DIR" -name "backup_*.sql.gz" -type f -printf "%T@ %p %s\n" | \
        sort -rn | \
        while IFS= read -r line; do
            local timestamp=$(echo "$line" | awk '{print $1}')
            local filepath=$(echo "$line" | awk '{print $2}')
            local filesize=$(echo "$line" | awk '{print $3}')
            local filename=$(basename "$filepath")
            local size_human=$(numfmt --to=iec-i --suffix=B "$filesize" 2>/dev/null || echo "${filesize}B")
            local date_human=$(date -d "@$timestamp" "+%Y-%m-%d %H:%M:%S" 2>/dev/null || echo "Unknown")
            
            echo "  - $filename ($size_human) - $date_human"
        done
    
    echo "========================================"
}

# Function to upload backup to remote storage (optional)
upload_to_remote() {
    print_info "Checking for remote backup configuration..."
    
    # Check if remote backup is configured
    if [ -n "${BACKUP_REMOTE_PATH:-}" ]; then
        print_info "Uploading backup to remote storage: $BACKUP_REMOTE_PATH"
        
        # Example: Using rsync to upload backup
        # rsync -avz "${BACKUP_DIR}/${BACKUP_FILE}" "$BACKUP_REMOTE_PATH/"
        
        # Example: Using AWS S3
        # aws s3 cp "${BACKUP_DIR}/${BACKUP_FILE}" "$BACKUP_REMOTE_PATH/"
        
        # Example: Using SCP
        # scp "${BACKUP_DIR}/${BACKUP_FILE}" "$BACKUP_REMOTE_PATH/"
        
        print_warning "Remote backup upload is not configured. Skipping."
    else
        print_info "No remote backup path configured. Skipping remote upload."
    fi
}

# Function to create checkpoint backup (before major changes)
create_checkpoint() {
    print_info "Creating checkpoint backup..."
    
    local checkpoint_file="checkpoint_${DATE_FORMAT}.sql.gz"
    
    cd "$PROJECT_DIR"
    
    docker-compose -f docker-compose.prod.yml exec -T postgres pg_dump \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        --no-owner \
        --no-acl \
        --clean \
        --if-exists \
        | gzip > "${BACKUP_DIR}/${checkpoint_file}"
    
    if [ $? -eq 0 ]; then
        print_success "Checkpoint backup created: $checkpoint_file"
    else
        print_error "Failed to create checkpoint backup."
    fi
}

# Main backup function
main() {
    print_info "Starting RegularReception Database Backup..."
    echo "========================================"
    
    # Check if docker-compose is running
    check_docker_compose
    
    # Load environment variables
    load_env
    
    # Create backup directory
    create_backup_dir
    
    # Create backup
    create_backup
    
    # Verify backup
    verify_backup
    
    # Create metadata
    create_metadata
    
    # Upload to remote storage (if configured)
    upload_to_remote
    
    # Cleanup old backups
    cleanup_old_backups
    
    # List all backups
    list_backups
    
    print_success "Backup completed successfully! 🎉"
    echo "========================================"
    print_info "Backup location: ${BACKUP_DIR}/${BACKUP_FILE}"
}

# Handle command line arguments
case "${1:-}" in
    --list)
        load_env
        list_backups
        ;;
    --checkpoint)
        check_docker_compose
        load_env
        create_backup_dir
        create_checkpoint
        ;;
    *)
        main "$@"
        ;;
esac

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
