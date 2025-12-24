# Scheduled Tasks Documentation

This document describes the automated scheduled tasks implemented in the Daryaft Core application.

## Overview

The application uses Spring's `@Scheduled` annotation to implement automated background tasks for managing subscription lifecycle and notifications. Scheduling is enabled via `@EnableScheduling` in the main application class.

## Schedulers

### 1. SubscriptionScheduler

Located in `com.daryaftmanazam.daryaftcore.scheduler.SubscriptionScheduler`

This scheduler manages the subscription lifecycle, handling overdue subscriptions and automatic deactivation.

#### Tasks

##### checkOverdueSubscriptions()
- **Schedule**: Daily at 2:00 AM (`0 0 2 * * *`)
- **Purpose**: Identifies and marks overdue subscriptions
- **Process**:
  1. Finds all ACTIVE subscriptions where `nextPaymentDate < today`
  2. Changes status to OVERDUE
  3. Sends overdue notification to customers
  4. Logs the process
- **Error Handling**: Continues processing even if individual subscriptions fail

##### autoDeactivateExpiredSubscriptions()
- **Schedule**: Daily at 3:00 AM (`0 0 3 * * *`)
- **Purpose**: Automatically expires subscriptions that have been overdue for too long
- **Process**:
  1. Finds all OVERDUE subscriptions older than 30 days
  2. Changes status to EXPIRED
  3. Deactivates customer if they have no other active subscriptions
  4. Sends final notice notification
- **Grace Period**: 30 days (configurable in application.yml)
- **Error Handling**: Continues processing even if individual subscriptions fail

### 2. NotificationScheduler

Located in `com.daryaftmanazam.daryaftcore.scheduler.NotificationScheduler`

This scheduler handles automated notifications and payment reminders.

#### Tasks

##### sendPaymentReminders()
- **Schedule**: Daily at 9:00 AM (`0 0 9 * * *`)
- **Purpose**: Sends payment reminders for upcoming due dates
- **Process**:
  1. Finds subscriptions with `nextPaymentDate` in the next 3 days
  2. Checks if reminder was already sent today (prevents duplicates)
  3. Sends reminder notifications
  4. Marks notifications as sent
- **Reminder Window**: 3 days before payment due date
- **Duplicate Prevention**: Won't send multiple reminders on the same day

##### processPendingNotifications()
- **Schedule**: Every 5 minutes (`fixedDelay = 300000` milliseconds)
- **Purpose**: Processes notifications that failed to send or are pending
- **Process**:
  1. Finds all notifications with PENDING status
  2. Attempts to send each notification
  3. Updates status to SENT (if successful) or FAILED (if unsuccessful)
  4. Records `sentAt` timestamp for successful notifications
- **Channels Supported**: EMAIL, SMS, PUSH_NOTIFICATION
- **Error Handling**: Marks failed notifications and continues processing

## Configuration

### Application Settings (application.yml)

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 5  # Thread pool size for scheduled tasks
      thread-name-prefix: "scheduler-"

scheduler:
  # Subscription Scheduler Settings
  subscription:
    overdue-check-cron: "0 0 2 * * *"  # Daily at 2 AM
    expire-check-cron: "0 0 3 * * *"   # Daily at 3 AM
    overdue-grace-period-days: 30      # Days before subscription expires
  
  # Notification Scheduler Settings
  notification:
    payment-reminder-cron: "0 0 9 * * *"        # Daily at 9 AM
    pending-process-fixed-delay: 300000         # Every 5 minutes (300000 ms)
    payment-reminder-days-before: 3             # Send reminder 3 days before payment due

logging:
  level:
    com.daryaftmanazam.daryaftcore.scheduler: INFO
    org.springframework.scheduling: DEBUG
```

### Cron Expression Format

```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12)
│ │ │ │ │ ┌───────────── day of week (0-7, Sun=0 or 7)
│ │ │ │ │ │
* * * * * *
```

## Notification Types

The following notification types are supported:

- `PAYMENT_REMINDER` - Sent 3 days before payment due
- `OVERDUE_NOTICE` - Sent when subscription becomes overdue
- `SUBSCRIPTION_EXPIRED` - Sent when subscription is expired after grace period
- `PAYMENT_CONFIRMATION` - Sent when payment is received
- `PAYMENT_SUCCESS` - Sent for successful payments
- `PAYMENT_FAILED` - Sent for failed payments

## Status Flow

### Subscription Status Flow
```
ACTIVE → OVERDUE (when payment date passed) → EXPIRED (after 30 days)
```

### Notification Status Flow
```
PENDING → SENT (successful delivery)
       → FAILED (delivery failed)
```

## Testing

Tests are provided for all scheduled methods. Tests call the scheduled methods directly without waiting for the schedule to trigger.

### Test Files
- `SubscriptionSchedulerTest.java` - Tests for subscription lifecycle automation
- `NotificationSchedulerTest.java` - Tests for notification automation

### Running Tests

```bash
./mvnw test -Dtest=SubscriptionSchedulerTest
./mvnw test -Dtest=NotificationSchedulerTest
```

## Monitoring

### Logging

All scheduled tasks provide comprehensive logging:

- **INFO level**: Task start/completion, counts of processed items
- **DEBUG level**: Individual item processing details
- **ERROR level**: Failures and exceptions

### Log Examples

```
[INFO] Starting scheduled task: checkOverdueSubscriptions
[INFO] Found 5 active subscriptions to check
[INFO] Marking subscription 123 as OVERDUE. Next payment was due on: 2025-12-20
[INFO] Completed checkOverdueSubscriptions task. Processed: 2, Errors: 0
```

## Performance Considerations

1. **Thread Pool**: Configured with 5 threads by default
2. **Batch Processing**: Each scheduler processes items in batches
3. **Error Isolation**: Failures in individual items don't stop the entire batch
4. **Transaction Management**: Each item is processed in its own transaction

## Customization

To customize the schedule:

1. Modify cron expressions in `application.yml`
2. Adjust grace periods and reminder windows
3. Change thread pool size based on load

## Troubleshooting

### Scheduler Not Running
- Verify `@EnableScheduling` is present in main application class
- Check if scheduled tasks are being picked up in logs
- Ensure application is running continuously

### Duplicate Notifications
- Check if multiple instances of the application are running
- Verify duplicate prevention logic in `sendPaymentReminders()`

### Performance Issues
- Increase thread pool size in `application.yml`
- Add database indexes on frequently queried fields
- Consider breaking large batches into smaller chunks

## Future Enhancements

Potential improvements:

1. Add distributed scheduling support (e.g., using Quartz Scheduler)
2. Implement notification rate limiting
3. Add retry mechanism for failed notifications
4. Provide admin UI for monitoring scheduled tasks
5. Add metrics and monitoring (e.g., using Micrometer)
