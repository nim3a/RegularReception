package com.daryaftmanazam.daryaftcore.scheduler;

import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import com.daryaftmanazam.daryaftcore.service.sms.SmsResult;
import com.daryaftmanazam.daryaftcore.service.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for sending SMS reminders about subscription expiry and overdue payments.
 * Runs automated tasks at scheduled times to notify customers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SmsService smsService;

    /**
     * Send reminders for subscriptions expiring in 3 days.
     * Runs every day at 09:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendExpiryReminders() {
        log.info("🔔 Starting subscription expiry reminder job...");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate reminderDate = today.plusDays(3);
            
            List<Subscription> expiringSubscriptions = 
                    subscriptionRepository.findActiveSubscriptionsEndingOn(reminderDate);
            
            log.info("Found {} subscriptions expiring on {}", 
                    expiringSubscriptions.size(), reminderDate);
            
            int successCount = 0;
            int failCount = 0;
            
            for (Subscription subscription : expiringSubscriptions) {
                try {
                    // Check if reminder already sent today
                    if (isReminderSentToday(subscription)) {
                        log.debug("Reminder already sent today for subscription {}", subscription.getId());
                        continue;
                    }
                    
                    Customer customer = subscription.getCustomer();
                    String message = buildExpiryReminderMessage(customer, subscription, reminderDate);
                    
                    SmsResult result = smsService.sendSms(customer.getPhone(), message);
                    
                    if (result.isSuccess()) {
                        successCount++;
                        // Update subscription with reminder sent flag
                        subscription.setLastReminderSent(LocalDateTime.now());
                        subscriptionRepository.save(subscription);
                        log.debug("Sent expiry reminder for subscription {}", subscription.getId());
                    } else {
                        failCount++;
                        log.warn("Failed to send reminder for subscription {}: {}", 
                                subscription.getId(), result.getErrorMessage());
                    }
                    
                    // Delay between SMS to avoid rate limiting
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("Error sending reminder for subscription {}", 
                            subscription.getId(), e);
                }
            }
            
            log.info("✅ Reminder job completed. Success: {}, Failed: {}", 
                    successCount, failCount);
                    
        } catch (Exception e) {
            log.error("Error in reminder scheduler", e);
        }
    }

    /**
     * Send reminders for overdue payments.
     * Runs every day at 10:00 AM.
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendOverduePaymentReminders() {
        log.info("💰 Starting overdue payment reminder job...");
        
        try {
            LocalDate today = LocalDate.now();
            List<Subscription> overdueSubscriptions = 
                    subscriptionRepository.findOverdueSubscriptions(today);
            
            log.info("Found {} overdue subscriptions", overdueSubscriptions.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (Subscription subscription : overdueSubscriptions) {
                // Skip if reminder already sent today
                if (isReminderSentToday(subscription)) {
                    log.debug("Overdue reminder already sent today for subscription {}", 
                            subscription.getId());
                    continue;
                }
                
                try {
                    Customer customer = subscription.getCustomer();
                    BigDecimal remainingAmount = calculateRemainingAmount(subscription);
                    String message = buildOverdueReminderMessage(customer, subscription, remainingAmount);
                    
                    SmsResult result = smsService.sendSms(customer.getPhone(), message);
                    
                    if (result.isSuccess()) {
                        successCount++;
                        subscription.setLastReminderSent(LocalDateTime.now());
                        subscriptionRepository.save(subscription);
                        log.debug("Sent overdue reminder for subscription {}", subscription.getId());
                    } else {
                        failCount++;
                        log.warn("Failed to send overdue reminder for subscription {}: {}", 
                                subscription.getId(), result.getErrorMessage());
                    }
                    
                    // Delay between SMS to avoid rate limiting
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("Error sending overdue reminder for subscription {}", 
                            subscription.getId(), e);
                }
            }
            
            log.info("✅ Overdue reminder job completed. Success: {}, Failed: {}", 
                    successCount, failCount);
                    
        } catch (Exception e) {
            log.error("Error in overdue payment scheduler", e);
        }
    }

    /**
     * Check if a reminder was already sent today for this subscription.
     *
     * @param subscription the subscription to check
     * @return true if reminder was sent today, false otherwise
     */
    private boolean isReminderSentToday(Subscription subscription) {
        if (subscription.getLastReminderSent() == null) {
            return false;
        }
        return subscription.getLastReminderSent().toLocalDate().equals(LocalDate.now());
    }

    /**
     * Build SMS message for expiring subscription.
     *
     * @param customer the customer
     * @param subscription the subscription
     * @param expiryDate the expiry date
     * @return formatted SMS message
     */
    private String buildExpiryReminderMessage(Customer customer, Subscription subscription, 
                                              LocalDate expiryDate) {
        return String.format(
                "مشتری گرامی %s، اشتراک شما در تاریخ %s به پایان می‌رسد. " +
                "لطفاً برای تمدید اشتراک اقدام فرمایید.",
                customer.getFullName(),
                expiryDate
        );
    }

    /**
     * Build SMS message for overdue payment.
     *
     * @param customer the customer
     * @param subscription the subscription
     * @param remainingAmount the remaining amount to pay
     * @return formatted SMS message
     */
    private String buildOverdueReminderMessage(Customer customer, Subscription subscription, 
                                               BigDecimal remainingAmount) {
        return String.format(
                "مشتری گرامی %s، اشتراک شما منقضی شده است. " +
                "لطفاً در اسرع وقت نسبت به تمدید اقدام نمایید. " +
                "مبلغ قابل پرداخت: %,d تومان",
                customer.getFullName(),
                remainingAmount.longValue()
        );
    }

    /**
     * Calculate remaining amount to be paid for a subscription.
     * This is total amount minus sum of successful payments.
     *
     * @param subscription the subscription
     * @return remaining amount
     */
    private BigDecimal calculateRemainingAmount(Subscription subscription) {
        BigDecimal totalAmount = subscription.getTotalAmount();
        BigDecimal paidAmount = subscription.getPayments().stream()
                .filter(payment -> "SUCCESS".equals(payment.getStatus().name()))
                .map(payment -> payment.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalAmount.subtract(paidAmount);
    }
}
