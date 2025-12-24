package com.daryaftmanazam.daryaftcore.scheduler;

import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Notification;
import com.daryaftmanazam.daryaftcore.model.Notification.NotificationStatus;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.NotificationRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import com.daryaftmanazam.daryaftcore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler component for managing notification operations.
 * Handles payment reminders and pending notification processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    
    private static final int PAYMENT_REMINDER_DAYS = 3;

    /**
     * Scheduled task to send payment reminders.
     * Runs daily at 9 AM.
     * 
     * Finds all active subscriptions with nextPaymentDate in the next 3 days
     * and sends reminder notifications.
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendPaymentReminders() {
        log.info("Starting scheduled task: sendPaymentReminders");
        
        LocalDate today = LocalDate.now();
        LocalDate reminderThreshold = today.plusDays(PAYMENT_REMINDER_DAYS);
        int sentCount = 0;
        int errorCount = 0;
        
        try {
            // Find all ACTIVE subscriptions
            List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
            
            log.info("Found {} active subscriptions to check for payment reminders", 
                    activeSubscriptions.size());
            
            for (Subscription subscription : activeSubscriptions) {
                try {
                    LocalDate nextPaymentDate = subscription.getNextPaymentDate();
                    
                    // Check if payment is due in the next 3 days
                    if (nextPaymentDate != null && 
                        !nextPaymentDate.isBefore(today) && 
                        !nextPaymentDate.isAfter(reminderThreshold)) {
                        
                        log.info("Sending payment reminder for subscription {} with next payment on {}", 
                                subscription.getId(), nextPaymentDate);
                        
                        Customer customer = subscription.getCustomer();
                        
                        // Check if reminder was already sent today
                        boolean reminderSentToday = notificationRepository
                                .findByCustomerIdAndSubscriptionIdAndSentAtAfter(
                                        customer.getId(), 
                                        subscription.getId(), 
                                        today.atStartOfDay())
                                .stream()
                                .anyMatch(n -> n.getNotificationType() == 
                                        com.daryaftmanazam.daryaftcore.model.enums.NotificationType.PAYMENT_REMINDER);
                        
                        if (!reminderSentToday) {
                            // Send reminder notification
                            notificationService.sendPaymentReminder(customer.getId(), subscription.getId());
                            sentCount++;
                            log.debug("Successfully sent payment reminder for subscription: {}", 
                                    subscription.getId());
                        } else {
                            log.debug("Reminder already sent today for subscription: {}", 
                                    subscription.getId());
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error sending payment reminder for subscription {}: {}", 
                            subscription.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Completed sendPaymentReminders task. Sent: {}, Errors: {}", 
                    sentCount, errorCount);
            
        } catch (Exception e) {
            log.error("Fatal error in sendPaymentReminders task: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled task to process pending notifications.
     * Runs every 5 minutes with a fixed delay.
     * 
     * Finds all notifications with PENDING status, attempts to send them,
     * and updates their status accordingly.
     */
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void processPendingNotifications() {
        log.debug("Starting scheduled task: processPendingNotifications");
        
        int processedCount = 0;
        int successCount = 0;
        int failedCount = 0;
        
        try {
            // Find all PENDING notifications
            List<Notification> pendingNotifications = notificationRepository.findPendingNotifications();
            
            if (pendingNotifications.isEmpty()) {
                log.debug("No pending notifications to process");
                return;
            }
            
            log.info("Found {} pending notifications to process", pendingNotifications.size());
            
            for (Notification notification : pendingNotifications) {
                try {
                    log.debug("Processing pending notification: {}", notification.getId());
                    
                    Customer customer = notification.getCustomer();
                    
                    // Attempt to send the notification
                    boolean sent = attemptToSendNotification(notification, customer);
                    
                    if (sent) {
                        // Update status to SENT
                        notification.setStatus(NotificationStatus.SENT);
                        notification.setSentAt(LocalDateTime.now());
                        successCount++;
                        log.debug("Successfully sent pending notification: {}", notification.getId());
                    } else {
                        // Update status to FAILED
                        notification.setStatus(NotificationStatus.FAILED);
                        failedCount++;
                        log.warn("Failed to send pending notification: {}", notification.getId());
                    }
                    
                    notificationRepository.save(notification);
                    processedCount++;
                    
                } catch (Exception e) {
                    log.error("Error processing pending notification {}: {}", 
                            notification.getId(), e.getMessage(), e);
                    
                    // Mark as failed
                    try {
                        notification.setStatus(NotificationStatus.FAILED);
                        notificationRepository.save(notification);
                        failedCount++;
                    } catch (Exception saveError) {
                        log.error("Error updating notification status: {}", saveError.getMessage());
                    }
                }
            }
            
            log.info("Completed processPendingNotifications task. Processed: {}, Succeeded: {}, Failed: {}", 
                    processedCount, successCount, failedCount);
            
        } catch (Exception e) {
            log.error("Fatal error in processPendingNotifications task: {}", e.getMessage(), e);
        }
    }

    /**
     * Attempts to send a notification through the appropriate channel.
     * This is a mock implementation - in production, this would integrate with
     * actual email/SMS services.
     * 
     * @param notification the notification to send
     * @param customer the customer to send to
     * @return true if successfully sent, false otherwise
     */
    private boolean attemptToSendNotification(Notification notification, Customer customer) {
        try {
            switch (notification.getChannel()) {
                case EMAIL:
                    // Mock email sending
                    if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                        log.debug("Sending email to: {}", customer.getEmail());
                        // Simulate email sending logic
                        return true;
                    }
                    break;
                    
                case SMS:
                    // Mock SMS sending
                    if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().isEmpty()) {
                        log.debug("Sending SMS to: {}", customer.getPhoneNumber());
                        // Simulate SMS sending logic
                        return true;
                    }
                    break;
                    
                case PUSH_NOTIFICATION:
                    // Mock push notification
                    log.debug("Sending push notification to customer: {}", customer.getId());
                    // Simulate push notification logic
                    return true;
                    
                default:
                    log.warn("Unsupported notification channel: {}", notification.getChannel());
                    return false;
            }
            
            log.warn("Missing contact information for channel: {}", notification.getChannel());
            return false;
            
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            return false;
        }
    }
}
