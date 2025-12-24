# SMS Reminder System for Expiring Subscriptions

## Overview
This system automatically sends SMS reminders to customers for:
- Subscriptions expiring in 3 days (sent at 09:00 AM daily)
- Overdue payment reminders (sent at 10:00 AM daily)

## Implementation Components

### 1. Main Application
**File:** [src/main/java/com/daryaftmanazam/daryaftcore/DaryaftCoreApplication.java](src/main/java/com/daryaftmanazam/daryaftcore/DaryaftCoreApplication.java)

Already configured with `@EnableScheduling` annotation to enable scheduled tasks.

### 2. Subscription Reminder Scheduler
**File:** [src/main/java/com/daryaftmanazam/daryaftcore/scheduler/SubscriptionReminderScheduler.java](src/main/java/com/daryaftmanazam/daryaftcore/scheduler/SubscriptionReminderScheduler.java)

#### Features:
- ✅ **Expiry Reminders**: Runs daily at 09:00 AM, sends reminders 3 days before expiry
- ✅ **Overdue Reminders**: Runs daily at 10:00 AM, sends reminders for overdue payments
- ✅ **Idempotent**: Checks if reminder already sent today to prevent duplicates
- ✅ **Error Handling**: Gracefully handles errors without stopping the job
- ✅ **Rate Limiting**: 1-second delay between SMS sends
- ✅ **Comprehensive Logging**: Logs all operations with success/failure counts
- ✅ **Tracking**: Updates `lastReminderSent` timestamp after successful send

#### Scheduled Jobs:

**1. sendExpiryReminders()**
```java
@Scheduled(cron = "0 0 9 * * ?") // Every day at 09:00 AM
```
- Finds subscriptions ending in 3 days
- Sends Persian SMS: "مشتری گرامی [نام], اشتراک شما در تاریخ [تاریخ] به پایان می‌رسد..."
- Tracks reminder sent timestamp

**2. sendOverduePaymentReminders()**
```java
@Scheduled(cron = "0 0 10 * * ?") // Every day at 10:00 AM
```
- Finds overdue subscriptions with remaining balance
- Calculates remaining amount from payments
- Sends Persian SMS with amount due
- Prevents duplicate sends on same day

### 3. Repository Methods
**File:** [src/main/java/com/daryaftmanazam/daryaftcore/repository/SubscriptionRepository.java](src/main/java/com/daryaftmanazam/daryaftcore/repository/SubscriptionRepository.java)

Added three new query methods:

```java
// Find active subscriptions ending on specific date (all businesses)
List<Subscription> findActiveSubscriptionsEndingOn(LocalDate date);

// Find active subscriptions ending on specific date (by business)
List<Subscription> findActiveSubscriptionsEndingOn(LocalDate date, Long businessId);

// Find overdue subscriptions (already existed, enhanced)
List<Subscription> findOverdueSubscriptions(LocalDate today);
```

### 4. Subscription Entity
**File:** [src/main/java/com/daryaftmanazam/daryaftcore/model/Subscription.java](src/main/java/com/daryaftmanazam/daryaftcore/model/Subscription.java)

Added field:
```java
@Column(name = "last_reminder_sent")
private LocalDateTime lastReminderSent;
```

This tracks when the last reminder SMS was sent to prevent duplicates.

### 5. Database Migration
**File:** [database-migrations/add_sms_tracking_to_subscriptions.sql](database-migrations/add_sms_tracking_to_subscriptions.sql)

```sql
-- Add reminder tracking column
ALTER TABLE subscriptions
ADD COLUMN IF NOT EXISTS last_reminder_sent TIMESTAMP;

-- Performance indexes
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date 
ON subscriptions(end_date);

CREATE INDEX IF NOT EXISTS idx_subscriptions_status_end_date 
ON subscriptions(status, end_date);
```

## Key Features

### ✅ Idempotency
The system checks `lastReminderSent` to ensure reminders are sent only once per day:
```java
private boolean isReminderSentToday(Subscription subscription) {
    if (subscription.getLastReminderSent() == null) {
        return false;
    }
    return subscription.getLastReminderSent().toLocalDate().equals(LocalDate.now());
}
```

### ✅ Error Handling
- Individual subscription errors don't stop the job
- Comprehensive error logging with subscription ID
- Success/failure counts reported

### ✅ Rate Limiting
```java
Thread.sleep(1000); // 1 second delay between SMS
```

### ✅ Remaining Amount Calculation
```java
private BigDecimal calculateRemainingAmount(Subscription subscription) {
    BigDecimal totalAmount = subscription.getTotalAmount();
    BigDecimal paidAmount = subscription.getPayments().stream()
            .filter(payment -> "SUCCESS".equals(payment.getStatus().name()))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return totalAmount.subtract(paidAmount);
}
```

## SMS Message Templates

### Expiry Reminder (3 days before)
```
مشتری گرامی [نام مشتری]، اشتراک شما در تاریخ [تاریخ انقضا] به پایان می‌رسد. 
لطفاً برای تمدید اشتراک اقدام فرمایید.
```

### Overdue Payment Reminder
```
مشتری گرامی [نام مشتری]، اشتراک شما منقضی شده است. 
لطفاً در اسرع وقت نسبت به تمدید اقدام نمایید. 
مبلغ قابل پرداخت: [مبلغ] تومان
```

## Logging Examples

```
🔔 Starting subscription expiry reminder job...
Found 5 subscriptions expiring on 2025-12-26
✅ Reminder job completed. Success: 5, Failed: 0

💰 Starting overdue payment reminder job...
Found 3 overdue subscriptions
✅ Overdue reminder job completed. Success: 3, Failed: 0
```

## Database Schema Changes

```sql
subscriptions
├─ id (existing)
├─ customer_id (existing)
├─ end_date (existing)
├─ status (existing)
├─ total_amount (existing)
├─ last_reminder_sent (NEW) ← Tracks last SMS send time
└─ ... other fields

Indexes:
- idx_subscriptions_end_date (end_date)
- idx_subscriptions_status_end_date (status, end_date)
```

## Deployment Steps

1. **Apply Database Migration**
   ```bash
   psql -U username -d database_name -f database-migrations/add_sms_tracking_to_subscriptions.sql
   ```

2. **Build Application**
   ```bash
   mvn clean install
   ```

3. **Restart Application**
   The scheduler will automatically start and run at scheduled times.

## Testing

### Manual Test (Trigger immediately)
You can temporarily change the cron expression for testing:
```java
@Scheduled(cron = "0 * * * * ?") // Every minute
```

### Check Logs
Monitor the logs for scheduled execution:
```bash
tail -f logs/application.log | grep "reminder job"
```

### Verify SMS Sent
Check the `last_reminder_sent` column:
```sql
SELECT id, customer_id, end_date, last_reminder_sent 
FROM subscriptions 
WHERE last_reminder_sent IS NOT NULL
ORDER BY last_reminder_sent DESC;
```

## Configuration

### Cron Schedule Format
```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12)
│ │ │ │ │ ┌───────────── day of week (0-6)
│ │ │ │ │ │
* * * * * *
```

**Current Settings:**
- Expiry Reminders: `0 0 9 * * ?` → 09:00 AM daily
- Overdue Reminders: `0 0 10 * * ?` → 10:00 AM daily

### Customization Options

**Change reminder advance notice:**
```java
LocalDate reminderDate = today.plusDays(3); // Change 3 to desired days
```

**Change schedule time:**
```java
@Scheduled(cron = "0 0 8 * * ?") // Change hour to 8 for 08:00 AM
```

**Change delay between SMS:**
```java
Thread.sleep(2000); // Change to 2 seconds
```

## Dependencies

This implementation uses:
- Spring Boot Scheduling (`@EnableScheduling`, `@Scheduled`)
- Spring Data JPA (for repository queries)
- Lombok (for logging and constructors)
- SMS Service (existing integration)

## Security & Performance

### Security
- ✅ No sensitive data logged (only IDs and counts)
- ✅ Customer phone numbers not logged in plain text
- ✅ SMS service handles authentication

### Performance
- ✅ Indexed queries for fast subscription lookups
- ✅ Batch processing with error isolation
- ✅ Rate limiting prevents API throttling
- ✅ Efficient JPA queries with proper joins

## Troubleshooting

### No SMS Sent
1. Check SMS service configuration in `application.yml`
2. Verify `SmsService` bean is available
3. Check SMS provider balance/quota
4. Review logs for error messages

### Duplicate SMS
- The `lastReminderSent` check should prevent this
- Verify database transactions are committing properly
- Check system time/timezone settings

### Performance Issues
- Monitor database query performance
- Consider pagination for large subscription counts
- Adjust rate limiting delay if needed

## Future Enhancements

Potential improvements:
- [ ] Configurable reminder advance days (via properties)
- [ ] Multiple reminder stages (7 days, 3 days, 1 day)
- [ ] SMS template management via database
- [ ] Retry mechanism for failed SMS
- [ ] Dashboard for reminder statistics
- [ ] Webhook notifications for reminder status

---

**Created:** December 23, 2025  
**Status:** ✅ Complete and Production Ready
