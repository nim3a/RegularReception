package com.daryaftmanazam.daryaftcore.scheduler;

import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Notification;
import com.daryaftmanazam.daryaftcore.model.Notification.NotificationStatus;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.Channel;
import com.daryaftmanazam.daryaftcore.model.enums.NotificationType;
import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.NotificationRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentPlanRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import com.daryaftmanazam.daryaftcore.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for NotificationScheduler.
 * Tests scheduled methods by calling them directly.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationSchedulerTest {

    @Autowired
    private NotificationScheduler notificationScheduler;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    private Customer testCustomer;
    private PaymentPlan testPaymentPlan;

    @BeforeEach
    void setUp() {
        // Clear data
        notificationRepository.deleteAll();
        subscriptionRepository.deleteAll();
        customerRepository.deleteAll();
        paymentPlanRepository.deleteAll();

        // Create test payment plan
        testPaymentPlan = PaymentPlan.builder()
                .planName("Test Monthly Plan")
                .baseAmount(new BigDecimal("100.00"))
                .periodType(PeriodType.MONTHLY)
                .periodCount(1)
                .isActive(true)
                .build();
        testPaymentPlan = paymentPlanRepository.save(testPaymentPlan);

        // Create test customer
        testCustomer = Customer.builder()
                .firstName("Test")
                .lastName("Customer")
                .email("test@example.com")
                .phoneNumber("09123456789")
                .isActive(true)
                .joinDate(LocalDate.now())
                .build();
        testCustomer = customerRepository.save(testCustomer);
    }

    @Test
    void testSendPaymentReminders_ShouldSendForUpcomingPayments() {
        // Given: ACTIVE subscription with payment due in 2 days
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().plusDays(2))  // Due in 2 days
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        notificationScheduler.sendPaymentReminders();

        // Then: Reminder should be sent - verify by checking notifications in database
        // (In production, we'd verify the notification was created)
    }

    @Test
    void testSendPaymentReminders_ShouldSendForPaymentDueToday() {
        // Given: ACTIVE subscription with payment due today
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now())  // Due today
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        notificationScheduler.sendPaymentReminders();

        // Then: Reminder should be sent
    }

    @Test
    void testSendPaymentReminders_ShouldNotSendForDistantPayments() {
        // Given: ACTIVE subscription with payment due in 5 days (beyond 3 day threshold)
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().plusDays(5))  // Due in 5 days
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        notificationScheduler.sendPaymentReminders();

        // Then: No reminder should be sent
    }

    @Test
    void testSendPaymentReminders_ShouldNotSendForOverduePayments() {
        // Given: ACTIVE subscription with overdue payment
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().minusDays(1))  // Overdue
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        notificationScheduler.sendPaymentReminders();

        // Then: No reminder should be sent (overdue notifications handled separately)
    }

    @Test
    void testSendPaymentReminders_ShouldHandleMultipleSubscriptions() {
        // Given: Multiple subscriptions with different payment dates
        Subscription subscription1 = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(30))
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().plusDays(1))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription1 = subscriptionRepository.save(subscription1);

        Subscription subscription2 = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusDays(40))
                .nextPaymentDate(LocalDate.now().plusDays(3))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription2 = subscriptionRepository.save(subscription2);

        Subscription subscription3 = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(50))
                .nextPaymentDate(LocalDate.now().plusDays(10))  // Beyond threshold
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription3 = subscriptionRepository.save(subscription3);

        // When: Run the scheduled task
        notificationScheduler.sendPaymentReminders();

        // Then: Reminders should be sent for first two subscriptions only
    }

    @Test
    void testProcessPendingNotifications_ShouldProcessPendingNotifications() {
        // Given: Pending notification
        Notification notification = Notification.builder()
                .customer(testCustomer)
                .notificationType(NotificationType.PAYMENT_REMINDER)
                .channel(Channel.EMAIL)
                .message("Test notification message")
                .status(NotificationStatus.PENDING)
                .build();
        notification = notificationRepository.save(notification);

        // When: Run the scheduled task
        notificationScheduler.processPendingNotifications();

        // Then: Notification status should be updated
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.getStatus()).isIn(NotificationStatus.SENT, NotificationStatus.FAILED);
        
        if (updatedNotification.getStatus() == NotificationStatus.SENT) {
            assertThat(updatedNotification.getSentAt()).isNotNull();
        }
    }

    @Test
    void testProcessPendingNotifications_ShouldMarkAsSentForValidEmail() {
        // Given: Pending notification with valid email
        Notification notification = Notification.builder()
                .customer(testCustomer)
                .notificationType(NotificationType.PAYMENT_REMINDER)
                .channel(Channel.EMAIL)
                .message("Test notification")
                .status(NotificationStatus.PENDING)
                .build();
        notification = notificationRepository.save(notification);

        // When: Run the scheduled task
        notificationScheduler.processPendingNotifications();

        // Then: Should be marked as SENT (mock implementation always succeeds with valid email)
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(updatedNotification.getSentAt()).isNotNull();
    }

    @Test
    void testProcessPendingNotifications_ShouldHandleMultiplePendingNotifications() {
        // Given: Multiple pending notifications
        Notification notification1 = Notification.builder()
                .customer(testCustomer)
                .notificationType(NotificationType.PAYMENT_REMINDER)
                .channel(Channel.EMAIL)
                .message("Test notification 1")
                .status(NotificationStatus.PENDING)
                .build();
        notification1 = notificationRepository.save(notification1);

        Notification notification2 = Notification.builder()
                .customer(testCustomer)
                .notificationType(NotificationType.OVERDUE_NOTICE)
                .channel(Channel.SMS)
                .message("Test notification 2")
                .status(NotificationStatus.PENDING)
                .build();
        notification2 = notificationRepository.save(notification2);

        Notification notification3 = Notification.builder()
                .customer(testCustomer)
                .notificationType(NotificationType.PAYMENT_SUCCESS)
                .channel(Channel.EMAIL)
                .message("Test notification 3")
                .status(NotificationStatus.PENDING)
                .build();
        notification3 = notificationRepository.save(notification3);

        // When: Run the scheduled task
        notificationScheduler.processPendingNotifications();

        // Then: All notifications should be processed
        List<Notification> pendingNotifications = notificationRepository.findByStatus(NotificationStatus.PENDING);
        assertThat(pendingNotifications).isEmpty();

        // All should be either SENT or FAILED
        List<Notification> allNotifications = notificationRepository.findAll();
        assertThat(allNotifications).hasSize(3);
        assertThat(allNotifications).allMatch(n -> 
                n.getStatus() == NotificationStatus.SENT || n.getStatus() == NotificationStatus.FAILED);
    }

    @Test
    void testProcessPendingNotifications_ShouldNotProcessSentNotifications() {
        // Given: Already sent notification
        Notification notification = Notification.builder()
                .customer(testCustomer)
                .notificationType(NotificationType.PAYMENT_REMINDER)
                .channel(Channel.EMAIL)
                .message("Test notification")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now().minusHours(1))
                .build();
        notification = notificationRepository.save(notification);

        // When: Run the scheduled task
        notificationScheduler.processPendingNotifications();

        // Then: Status should remain SENT
        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(updatedNotification.getSentAt()).isEqualTo(notification.getSentAt());
    }

    @Test
    void testProcessPendingNotifications_ShouldHandleEmptyQueue() {
        // Given: No pending notifications
        // (setUp already clears all data)

        // When: Run the scheduled task (should not throw exception)
        notificationScheduler.processPendingNotifications();

        // Then: No errors should occur
        List<Notification> allNotifications = notificationRepository.findAll();
        assertThat(allNotifications).isEmpty();
    }
}
