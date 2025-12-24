package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.exception.CustomerNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.PaymentNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.SubscriptionNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Notification;
import com.daryaftmanazam.daryaftcore.model.Notification.NotificationStatus;
import com.daryaftmanazam.daryaftcore.model.Payment;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.Channel;
import com.daryaftmanazam.daryaftcore.model.enums.NotificationType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.NotificationRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing notification operations.
 * Notifications are logged even if sending fails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    
    private static final int PAYMENT_REMINDER_DAYS_BEFORE = 3;
    
    /**
     * Send payment reminder notification.
     */
    @Transactional
    public void sendPaymentReminder(Long customerId, Long subscriptionId) {
        log.info("Sending payment reminder for customer {} and subscription {}", 
                customerId, subscriptionId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        String message = buildPaymentReminderMessage(customer, subscription);
        
        createNotification(customer, subscription, NotificationType.PAYMENT_REMINDER, message);
        
        // Send notification via email/SMS (mock implementation)
        sendEmail(customer.getEmail(), "Payment Reminder", message);
    }
    
    /**
     * Send overdue notification.
     */
    @Transactional
    public void sendOverdueNotification(Long customerId, Long subscriptionId) {
        log.info("Sending overdue notification for customer {} and subscription {}", 
                customerId, subscriptionId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        String message = buildOverdueMessage(customer, subscription);
        
        createNotification(customer, subscription, NotificationType.OVERDUE_NOTICE, message);
        
        // Send notification via email/SMS (mock implementation)
        sendEmail(customer.getEmail(), "Overdue Payment Notice", message);
    }
    
    /**
     * Send payment confirmation notification.
     */
    @Transactional
    public void sendPaymentConfirmation(Long paymentId) {
        log.info("Sending payment confirmation for payment {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        
        Customer customer = payment.getSubscription().getCustomer();
        
        String message = buildPaymentConfirmationMessage(customer, payment);
        
        createNotification(customer, payment.getSubscription(), 
                NotificationType.PAYMENT_CONFIRMATION, message);
        
        // Send notification via email/SMS (mock implementation)
        sendEmail(customer.getEmail(), "Payment Confirmation", message);
    }
    
    /**
     * Send subscription expired notification.
     */
    @Transactional
    public void sendSubscriptionExpiredNotification(Long customerId, Long subscriptionId) {
        log.info("Sending subscription expired notification for customer {} and subscription {}", 
                customerId, subscriptionId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        String message = buildSubscriptionExpiredMessage(customer, subscription);
        
        createNotification(customer, subscription, NotificationType.SUBSCRIPTION_EXPIRED, message);
        
        // Send notification via email/SMS (mock implementation)
        sendEmail(customer.getEmail(), "Subscription Expired", message);
    }
    
    /**
     * Create a notification record.
     */
    private void createNotification(Customer customer, Subscription subscription, 
                                   NotificationType type, String message) {
        Channel channel = determineChannel(customer);
        
        Notification notification = Notification.builder()
                .customer(customer)
                .subscription(subscription)
                .notificationType(type)
                .channel(channel)
                .message(message)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.SENT)
                .build();
        
        notificationRepository.save(notification);
        log.debug("Notification created: type={}, customer={}, subscription={}", 
                type, customer.getId(), subscription.getId());
    }
    
    /**
     * Send email notification (mock implementation for MVP).
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Mock email sent to: {}", to);
        log.debug("Subject: {}, Body: {}", subject, body);
        
        // Mock implementation - in real system, integrate with email service
        // e.g., JavaMailSender, SendGrid, AWS SES, etc.
        
        // Simulate sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Email sent successfully (mock)");
    }
    
    /**
     * Build payment reminder message.
     */
    private String buildPaymentReminderMessage(Customer customer, Subscription subscription) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return String.format(
                "سلام %s عزیز،\n\n" +
                "یادآوری پرداخت: تاریخ پرداخت بعدی شما %s می‌باشد.\n" +
                "مبلغ قابل پرداخت: %s تومان\n" +
                "نام پلن: %s\n\n" +
                "لطفاً نسبت به پرداخت اقدام فرمایید.\n\n" +
                "با تشکر",
                customer.getFirstName(),
                subscription.getNextPaymentDate().format(formatter),
                subscription.getPaymentPlan().getBaseAmount(),
                subscription.getPaymentPlan().getPlanName()
        );
    }
    
    /**
     * Build overdue message.
     */
    private String buildOverdueMessage(Customer customer, Subscription subscription) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int daysOverdue = (int) ChronoUnit.DAYS.between(
                subscription.getNextPaymentDate(), LocalDate.now());
        
        return String.format(
                "سلام %s عزیز،\n\n" +
                "پرداخت شما معوق شده است!\n" +
                "تاریخ سررسید: %s (%d روز گذشته)\n" +
                "مبلغ قابل پرداخت: %s تومان\n" +
                "نام پلن: %s\n\n" +
                "لطفاً در اسرع وقت نسبت به پرداخت اقدام فرمایید.\n" +
                "در صورت عدم پرداخت، جریمه دیرکرد اعمال خواهد شد.\n\n" +
                "با تشکر",
                customer.getFirstName(),
                subscription.getNextPaymentDate().format(formatter),
                daysOverdue,
                subscription.getPaymentPlan().getBaseAmount(),
                subscription.getPaymentPlan().getPlanName()
        );
    }
    
    /**
     * Build subscription expired message.
     */
    private String buildSubscriptionExpiredMessage(Customer customer, Subscription subscription) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return String.format(
                "سلام %s عزیز،\n\n" +
                "اشتراک شما منقضی شده است!\n" +
                "نام پلن: %s\n" +
                "تاریخ شروع: %s\n" +
                "تاریخ پایان: %s\n\n" +
                "اشتراک شما به دلیل عدم پرداخت به مدت طولانی غیرفعال شده است.\n" +
                "برای تمدید یا فعال‌سازی مجدد، لطفاً با ما تماس بگیرید.\n\n" +
                "با تشکر",
                customer.getFirstName(),
                subscription.getPaymentPlan().getPlanName(),
                subscription.getStartDate().format(formatter),
                subscription.getEndDate().format(formatter)
        );
    }
    
    /**
     * Build payment confirmation message.
     */
    private String buildPaymentConfirmationMessage(Customer customer, Payment payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        return String.format(
                "سلام %s عزیز،\n\n" +
                "پرداخت شما با موفقیت ثبت شد!\n" +
                "مبلغ پرداخت شده: %s تومان\n" +
                "تاریخ پرداخت: %s\n" +
                "شماره تراکنش: %s\n" +
                "روش پرداخت: %s\n\n" +
                "از حمایت شما سپاسگزاریم.\n\n" +
                "با تشکر",
                customer.getFirstName(),
                payment.getAmount(),
                payment.getPaymentDate().format(formatter),
                payment.getTransactionId(),
                payment.getPaymentMethod()
        );
    }
    
    /**
     * Determine notification channel based on customer preferences.
     */
    private Channel determineChannel(Customer customer) {
        // For MVP, default to EMAIL if customer has email, otherwise SMS
        if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
            return Channel.EMAIL;
        }
        return Channel.SMS;
    }
}
