# Flyway Database Migrations

This directory contains all database migration scripts for the RegularReception project.

## Migration Files

### V1__create_businesses_table.sql
- Creates the `businesses` table
- Core table for storing business information
- Includes indexes for phone number lookups

### V2__create_customers_table.sql
- Creates the `customers` table
- Links to businesses table with foreign key
- Includes indexes for business_id, phone, and email lookups

### V3__create_payment_plans_table.sql
- Creates the `payment_plans` table
- Defines subscription plans with pricing and duration
- Includes validation constraints for amounts and percentages

### V4__create_subscriptions_table.sql
- Creates the `subscriptions` table
- Links customers to payment plans
- Tracks subscription status and payment dates
- Includes indexes optimized for business queries

### V5__create_payments_table.sql
- Creates the `payments` table
- Records all payment transactions
- Links to subscriptions
- Includes transaction tracking and late fee support

### V6__create_users_table.sql
- Creates the `users` table for authentication
- Links users to businesses
- Creates default admin user (username: admin, password: admin123)

### V7__add_sms_config_to_businesses.sql
- Adds SMS configuration columns to businesses table
- Enables SMS notification feature per business

## Running Migrations

### Using Maven
```bash
# Check migration status
mvn flyway:info

# Run migrations
mvn flyway:migrate

# Repair migration history (if needed)
mvn flyway:repair

# Clean database (CAUTION: removes all data)
mvn flyway:clean
```

### Automatic Migration
Migrations run automatically when the application starts with:
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
```

## Migration Naming Convention

- **V{version}\_\_{description}.sql** - Versioned migrations (e.g., V1__create_businesses_table.sql)
- **U{version}\_\_{description}.sql** - Undo migrations (optional)
- **R\_\_{description}.sql** - Repeatable migrations (run on checksum change)

## Important Notes

1. **Never modify existing migration files** after they've been applied
2. Create new migration files for schema changes
3. Flyway tracks applied migrations in `flyway_schema_history` table
4. Migration files must use forward slashes in SQL comments
5. Use `baseline-on-migrate: true` for existing databases

## Configuration

### Development (application.yml)
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Changed from 'update'
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### Production (application-prod.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
```

## Troubleshooting

### Migration Failed
```bash
# Check migration status
mvn flyway:info

# Repair if needed
mvn flyway:repair
```

### Baseline Existing Database
If you have an existing database with tables:
```bash
mvn flyway:baseline
```

### Reset Database (Development Only)
```bash
# WARNING: This removes ALL data
mvn flyway:clean
mvn flyway:migrate
```

## Schema Version History

The `flyway_schema_history` table tracks all applied migrations:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Next Steps

After creating migrations:
1. ✅ Migration files created
2. ✅ application.yml updated (ddl-auto: validate)
3. ⏳ Run: `mvn flyway:migrate` or start the application
4. ⏳ Verify: `mvn flyway:info`
