# Sample Data Initialization Guide

This project includes two methods for initializing sample data in the database. You can choose either method based on your preference.

## 📊 Sample Data Overview

The sample data includes:

### 🏢 Businesses (2)
1. **باشگاه بی رانرز** (Be Runners Gym)
   - Owner: ویدا مختاری
   - Type: Fitness/Sports facility
   
2. **مجتمع مسکونی پارسیان** (Parsian Residential Complex)
   - Owner: محمد رضایی
   - Type: Residential complex

### 💳 Payment Plans (3 per business)
- **Monthly**: 500,000 Toman (no discount)
- **Quarterly**: 1,350,000 Toman (10% discount)
- **Semi-annual**: 2,550,000 Toman (15% discount)

### 👥 Customers (5 per business - 10 total)
Persian names with realistic Iranian data:
- Various customer types: NEW, REGULAR, VIP
- Join dates spread over the last 6 months

### 📝 Subscriptions (2-3 per customer)
Mixed statuses:
- **ACTIVE**: Current active subscriptions
- **OVERDUE**: Past due subscriptions with late fees
- **PENDING**: Awaiting payment
- **EXPIRED**: Completed/past subscriptions
- **CANCELLED**: Cancelled subscriptions

### 💰 Payment Records
Comprehensive payment history matching subscription timelines

---

## Method 1: DataInitializer Component (Default - Recommended)

### ✅ How It Works
The `DataInitializer` component automatically runs when the application starts and the database is empty.

### 🎯 Features
- **Automatic**: Runs on application startup
- **Smart**: Only initializes if database is empty
- **Logged**: Provides detailed logging of the initialization process
- **Flexible**: Easy to customize in code
- **Safe**: Transactional - rolls back on errors

### 📝 Usage
Simply run the application:

```bash
# Using Maven wrapper
.\mvnw.cmd spring-boot:run

# Or using the batch file
.\run.bat
```

### 🔍 What You'll See
The initialization logs will show:
```
Database is empty. Starting data initialization...
📊 Creating businesses...
✓ Created business: باشگاه بی رانرز (Owner: ویدا مختاری)
✓ Created business: مجتمع مسکونی پارسیان (Owner: محمد رضایی)
💳 Creating payment plans...
✓ Created 3 payment plans for: باشگاه بی رانرز
✓ Created 3 payment plans for: مجتمع مسکونی پارسیان
👥 Creating customers...
✓ Created 5 customers for: باشگاه بی رانرز
✓ Created 5 customers for: مجتمع مسکونی پارسیان
📝 Creating subscriptions...
✓ Created 22 subscriptions across all customers
💰 Creating payment records...
✓ Created 45 payment records
═══════════════════════════════════════════════════════
📊 DATA INITIALIZATION SUMMARY
═══════════════════════════════════════════════════════
🏢 Businesses created: 2
👥 Customers created: 10
💳 Payment plans created: 6
📝 Subscriptions created: 22
💰 Payments created: 45
```

### ⚙️ Configuration
Located at: `src/main/java/com/daryaftmanazam/daryaftcore/config/DataInitializer.java`

To disable, comment out `@Component` annotation or delete the file.

---

## Method 2: SQL Script (data.sql)

### ✅ How It Works
Executes SQL INSERT statements directly into the database using Spring's SQL initialization feature.

### 🎯 Features
- **Explicit**: Clear SQL statements
- **Database-native**: Uses database functions
- **Predictable**: Same data every time
- **Simple**: No Java code required

### 📝 Usage

1. **Enable data.sql execution** in `application.yml`:
```yaml
spring:
  sql:
    init:
      mode: always  # Change from 'never' to 'always'
```

2. **Disable DataInitializer**:
   - Either delete the `DataInitializer.java` file
   - Or comment out the `@Component` annotation

3. **Run the application**:
```bash
.\mvnw.cmd spring-boot:run
```

### 📍 File Location
`src/main/resources/data.sql`

### ⚠️ Important Notes
- Data is inserted every time the application starts
- To avoid duplicates, either:
  - Use `ddl-auto: create-drop` (recreates database each time)
  - Manually clear the database before running
  - Add unique constraints and handle conflicts

### 🔄 Switching Between Methods

**Current Configuration**: DataInitializer is ACTIVE, data.sql is DISABLED

To switch to data.sql:

1. Edit `application.yml`:
```yaml
spring:
  sql:
    init:
      mode: always  # Uncomment or change from 'never'
```

2. Disable DataInitializer (choose one):
   - Comment out `@Component` in `DataInitializer.java`
   - Delete the `DataInitializer.java` file

---

## 🧪 Testing the Data

### Access H2 Console
1. Start the application
2. Open browser: http://localhost:8081/h2-console
3. Login with:
   - **JDBC URL**: `jdbc:h2:file:./data/daryaftdb`
   - **Username**: `sa`
   - **Password**: (leave empty)

### Sample Queries

```sql
-- Check all businesses
SELECT * FROM businesses;

-- Check customers by business
SELECT c.first_name, c.last_name, c.phone_number, c.customer_type, b.business_name
FROM customers c
JOIN businesses b ON c.business_id = b.id
ORDER BY b.business_name, c.first_name;

-- Check active subscriptions with payment plans
SELECT 
    c.first_name || ' ' || c.last_name AS customer_name,
    pp.plan_name,
    s.status,
    s.total_amount,
    s.next_payment_date
FROM subscriptions s
JOIN customers c ON s.customer_id = c.id
JOIN payment_plans pp ON s.payment_plan_id = pp.id
WHERE s.status = 'ACTIVE'
ORDER BY s.next_payment_date;

-- Check overdue subscriptions
SELECT 
    c.first_name || ' ' || c.last_name AS customer_name,
    b.business_name,
    s.status,
    s.next_payment_date,
    DATEDIFF('DAY', s.next_payment_date, CURRENT_DATE) AS days_overdue
FROM subscriptions s
JOIN customers c ON s.customer_id = c.id
JOIN businesses b ON c.business_id = b.id
WHERE s.status = 'OVERDUE'
ORDER BY s.next_payment_date;

-- Check payment history
SELECT 
    p.transaction_id,
    c.first_name || ' ' || c.last_name AS customer_name,
    p.amount,
    p.payment_date,
    p.status,
    p.late_fee,
    p.payment_method
FROM payments p
JOIN subscriptions s ON p.subscription_id = s.id
JOIN customers c ON s.customer_id = c.id
ORDER BY p.payment_date DESC;

-- Subscription statistics by status
SELECT 
    status,
    COUNT(*) AS count,
    SUM(total_amount) AS total_amount
FROM subscriptions
GROUP BY status;

-- Revenue by business
SELECT 
    b.business_name,
    COUNT(DISTINCT c.id) AS customer_count,
    COUNT(s.id) AS subscription_count,
    SUM(CASE WHEN s.status = 'ACTIVE' THEN s.total_amount ELSE 0 END) AS active_revenue
FROM businesses b
LEFT JOIN customers c ON b.id = c.business_id
LEFT JOIN subscriptions s ON c.id = s.customer_id
GROUP BY b.business_name;
```

---

## 🎨 Data Characteristics

### Realistic Dates
- All dates are within the last 6 months
- Payment dates align with subscription timelines
- Overdue subscriptions have realistic past-due dates

### Persian/Farsi Names
All customer names are authentic Iranian names:
- علی احمدی, زهرا محمدی, حسین کریمی, etc.

### Varied Subscription States
- Active subscriptions with upcoming payments
- Overdue subscriptions with accumulated late fees
- Pending subscriptions awaiting first payment
- Historical expired/cancelled subscriptions

### Payment Records
- Completed payments with transaction IDs
- Pending payments for overdue subscriptions
- Late fees calculated based on days overdue
- Payment methods: کارت بانکی (Bank Card) or نقدی (Cash)

---

## 🛠️ Troubleshooting

### Data Not Loading
1. Check logs for initialization messages
2. Verify database connection in `application.yml`
3. Ensure H2 database file permissions

### Duplicate Data
1. If using data.sql, change `ddl-auto` to `create-drop`
2. Or manually clear database before restart
3. Or use DataInitializer which checks for empty database

### Database Locked
- Close H2 Console before restarting application
- Delete `./data/daryaftdb.mv.db` file if necessary

### Persian Characters Not Showing
- Ensure UTF-8 encoding in H2 Console
- Check browser encoding settings
- Verify `spring.messages.encoding: UTF-8` in application.yml

---

## 📚 Additional Resources

- **Entity Models**: `src/main/java/com/daryaftmanazam/daryaftcore/model/`
- **Repositories**: `src/main/java/com/daryaftmanazam/daryaftcore/repository/`
- **Application Config**: `src/main/resources/application.yml`
- **Main Application**: `src/main/java/com/daryaftmanazam/daryaftcore/DaryaftCoreApplication.java`

---

## ✅ Verification Checklist

After running the application, verify:

- [ ] Application starts without errors
- [ ] Initialization logs appear (if using DataInitializer)
- [ ] H2 Console accessible at http://localhost:8081/h2-console
- [ ] Database contains 2 businesses
- [ ] Database contains 10 customers
- [ ] Database contains 6 payment plans
- [ ] Database contains 20+ subscriptions
- [ ] Database contains 40+ payment records
- [ ] Persian characters display correctly
- [ ] Dates are within last 6 months
- [ ] Subscription statuses are varied (ACTIVE, OVERDUE, PENDING, etc.)

---

**Happy Testing! 🚀**
