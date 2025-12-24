package com.daryaftmanazam.daryaftcore.scheduler;

import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import com.daryaftmanazam.daryaftcore.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler component for managing subscription lifecycle events.
 * Handles overdue subscriptions and automatic deactivation of expired subscriptions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    
    private static final int OVERDUE_GRACE_PERIOD_DAYS = 30;

    /**
     * Scheduled task to check and mark overdue subscriptions.
     * Runs daily at 2 AM.
     * 
     * Finds all ACTIVE subscriptions where nextPaymentDate is before today,
     * changes their status to OVERDUE, and sends overdue notifications.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void checkOverdueSubscriptions() {
        log.info("Starting scheduled task: checkOverdueSubscriptions");
        
        LocalDate today = LocalDate.now();
        int processedCount = 0;
        int errorCount = 0;
        
        try {
            // Find all ACTIVE subscriptions where nextPaymentDate < today
            List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
            
            log.info("Found {} active subscriptions to check", activeSubscriptions.size());
            
            for (Subscription subscription : activeSubscriptions) {
                try {
                    if (subscription.getNextPaymentDate() != null && 
                        subscription.getNextPaymentDate().isBefore(today)) {
                        
                        log.info("Marking subscription {} as OVERDUE. Next payment was due on: {}", 
                                subscription.getId(), subscription.getNextPaymentDate());
                        
                        // Change status to OVERDUE
                        subscription.setStatus(SubscriptionStatus.OVERDUE);
                        subscriptionRepository.save(subscription);
                        
                        // Send overdue notification
                        Customer customer = subscription.getCustomer();
                        notificationService.sendOverdueNotification(customer.getId(), subscription.getId());
                        
                        processedCount++;
                        log.debug("Successfully processed overdue subscription: {}", subscription.getId());
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error processing subscription {}: {}", subscription.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Completed checkOverdueSubscriptions task. Processed: {}, Errors: {}", 
                    processedCount, errorCount);
            
        } catch (Exception e) {
            log.error("Fatal error in checkOverdueSubscriptions task: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled task to automatically deactivate expired subscriptions.
     * Runs daily at 3 AM.
     * 
     * Finds all OVERDUE subscriptions that have been overdue for more than 30 days,
     * changes their status to EXPIRED, deactivates the customer if needed,
     * and sends final notice notifications.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void autoDeactivateExpiredSubscriptions() {
        log.info("Starting scheduled task: autoDeactivateExpiredSubscriptions");
        
        LocalDate today = LocalDate.now();
        int processedCount = 0;
        int errorCount = 0;
        
        try {
            // Find all OVERDUE subscriptions
            List<Subscription> overdueSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.OVERDUE);
            
            log.info("Found {} overdue subscriptions to check", overdueSubscriptions.size());
            
            for (Subscription subscription : overdueSubscriptions) {
                try {
                    if (subscription.getNextPaymentDate() != null) {
                        long daysOverdue = ChronoUnit.DAYS.between(subscription.getNextPaymentDate(), today);
                        
                        if (daysOverdue > OVERDUE_GRACE_PERIOD_DAYS) {
                            log.info("Expiring subscription {} which has been overdue for {} days", 
                                    subscription.getId(), daysOverdue);
                            
                            // Change status to EXPIRED
                            subscription.setStatus(SubscriptionStatus.EXPIRED);
                            subscriptionRepository.save(subscription);
                            
                            Customer customer = subscription.getCustomer();
                            
                            // Check if customer has any other active subscriptions
                            boolean hasActiveSubscriptions = customer.getSubscriptions().stream()
                                    .anyMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE);
                            
                            // Deactivate customer if no active subscriptions
                            if (!hasActiveSubscriptions && customer.getIsActive()) {
                                log.info("Deactivating customer {} - no active subscriptions remaining", 
                                        customer.getId());
                                customer.setIsActive(false);
                            }
                            
                            // Send final notice
                            notificationService.sendSubscriptionExpiredNotification(
                                    customer.getId(), subscription.getId());
                            
                            processedCount++;
                            log.debug("Successfully expired subscription: {}", subscription.getId());
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error processing expired subscription {}: {}", 
                            subscription.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Completed autoDeactivateExpiredSubscriptions task. Processed: {}, Errors: {}", 
                    processedCount, errorCount);
            
        } catch (Exception e) {
            log.error("Fatal error in autoDeactivateExpiredSubscriptions task: {}", e.getMessage(), e);
        }
    }
}
