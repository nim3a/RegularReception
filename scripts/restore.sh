#!/bin/bash

# ============================================
# Database Restore Script
# ============================================
# This script restores a PostgreSQL database
# from a compressed backup file
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

# Function to validate parameters
validate_parameters() {
    if [ -z "$1" ]; then
        print_error "No backup file specified!"
        echo ""
        echo "Usage: $0 <backup_file_path>"
        echo "Example: $0 ./backups/daryaft_backup_20231223_120000.sql.gz"
        exit 1
    fi
    
    BACKUP_FILE="$1"
}

# Function to check if backup file exists
check_file_exists() {
    if [ ! -f "$BACKUP_FILE" ]; then
        print_error "Backup file not found: $BACKUP_FILE"
        exit 1
    fi
    
    print_success "Backup file found: $BACKUP_FILE"
    
    # Display file information
    FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    FILE_DATE=$(stat -c %y "$BACKUP_FILE" 2>/dev/null || stat -f %Sm "$BACKUP_FILE" 2>/dev/null)
    
    echo ""
    echo "Backup File Details:"
    echo "  📁 File: $(basename "$BACKUP_FILE")"
    echo "  📊 Size: $FILE_SIZE"
    echo "  📅 Date: $FILE_DATE"
    echo ""
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
    if [ -z "$POSTGRES_DB" ] || [ -z "$POSTGRES_USER" ] || [ -z "$POSTGRES_PASSWORD" ]; then
        print_error "Required environment variables not set in .env file"
        exit 1
    fi
}

# Function to get user confirmation
get_confirmation() {
    print_warning "⚠️  WARNING: This will REPLACE the current database with the backup!"
    print_warning "All existing data will be lost!"
    echo ""
    read -p "Are you sure you want to continue? (yes/no): " confirmation
    
    if [ "$confirmation" != "yes" ]; then
        print_info "Restore cancelled by user"
        exit 0
    fi
    
    echo ""
    print_info "User confirmed. Proceeding with restore..."
}

# Function to check if database container is running
check_container() {
    print_info "Checking database container status..."
    
    if ! docker-compose -f docker-compose.prod.yml ps postgres | grep -q "Up"; then
        print_error "PostgreSQL container is not running!"
        print_info "Start it with: docker-compose -f docker-compose.prod.yml up -d postgres"
        exit 1
    fi
    
    print_success "PostgreSQL container is running"
}

# Function to perform database restore
perform_restore() {
    print_info "Starting database restore process..."
    
    # Decompress and restore the backup
    if gunzip -c "$BACKUP_FILE" | docker-compose -f docker-compose.prod.yml exec -T postgres psql \
        -U "$POSTGRES_USER" \
        -d postgres \
        --quiet; then
        
        print_success "Database restore completed successfully! 🎉"
        echo ""
        echo "Restore Details:"
        echo "  ✅ Database: $POSTGRES_DB"
        echo "  📁 From file: $(basename "$BACKUP_FILE")"
        echo "  👤 User: $POSTGRES_USER"
    else
        print_error "Database restore failed!"
        print_error "The database may be in an inconsistent state"
        exit 1
    fi
}

# Function to verify restore
verify_restore() {
    print_info "Verifying database connection..."
    
    # Test database connection
    if docker-compose -f docker-compose.prod.yml exec -T postgres psql \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        -c "SELECT version();" > /dev/null 2>&1; then
        
        print_success "Database connection verified"
    else
        print_error "Failed to connect to restored database"
        exit 1
    fi
}

# Main restore process
main() {
    echo "============================================"
    echo "🔄 Database Restore Process"
    echo "============================================"
    echo ""
    
    # Change to script directory's parent (project root)
    cd "$(dirname "$0")/.."
    
    validate_parameters "$1"
    check_file_exists
    load_env
    get_confirmation
    check_container
    perform_restore
    verify_restore
    
    echo ""
    echo "============================================"
    echo "✅ Restore process completed successfully!"
    echo "============================================"
    echo ""
    print_info "You may need to restart the application container:"
    echo "  docker-compose -f docker-compose.prod.yml restart app"
}

# Trap errors
trap 'print_error "An error occurred during restore. Database may be in an inconsistent state."; exit 1' ERR

# Execute main function with all arguments
main "$@"
