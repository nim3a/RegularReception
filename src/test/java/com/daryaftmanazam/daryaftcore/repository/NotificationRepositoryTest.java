package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.Notification.NotificationStatus;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.daryaftmanazam.daryaftcore.model.enums.NotificationType;
import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for NotificationRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private Business business;
    private Customer customer1;
    private Customer customer2;
    private PaymentPlan paymentPlan;
    private Subscription subscription;
    private Notification pendingNotification;
    private Notification sentNotification;
    private Notification failedNotification;

    @BeforeEach
    void setUp() {
        // Create business
        business = Business.builder()
                .businessName("Test Gym")
                .ownerName("Owner")
                .contactEmail("owner@gym.com")
                .contactPhone("09111111111")
                .isActive(true)
                .build();
        entityManager.persist(business);

        // Create payment plan
        paymentPlan = PaymentPlan.builder()
                .planName("Monthly Plan")
                .periodType(PeriodType.MONTHLY)
                .periodCount(1)
                .baseAmount(new BigDecimal("500000"))
                .isActive(true)
                .business(business)
                .build();
        entityManager.persist(paymentPlan);

        // Create customers
        customer1 = Customer.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .phoneNumber("09123456789")
                .email("ali@example.com")
                .customerType(CustomerType.REGULAR)
                .isActive(true)
                .business(business)
                .build();

        customer2 = Customer.builder()
                .firstName("Sara")
                .lastName("Karimi")
                .phoneNumber("09987654321")
                .email("sara@example.com")
                .customerType(CustomerType.VIP)
                .isActive(true)
                .business(business)
                .build();

        entityManager.persist(customer1);
        entityManager.persist(customer2);

        // Create subscription
        subscription = Subscription.builder()
                .customer(customer1)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("1350000"))
                .build();
        entityManager.persist(subscription);

        // Create notifications
        pendingNotification = Notification.builder()
                .customer(customer1)
                .subscription(subscription)
                .notificationType(NotificationType.REMINDER)
                .message("Your payment is due soon")
                .status(NotificationStatus.PENDING)
                .build();

        sentNotification = Notification.builder()
                .customer(customer1)
                .subscription(subscription)
                .notificationType(NotificationType.CONFIRMATION)
                .message("Payment received successfully")
                .sentAt(LocalDateTime.now().minusDays(2))
                .status(NotificationStatus.SENT)
                .build();

        failedNotification = Notification.builder()
                .customer(customer2)
                .notificationType(NotificationType.WARNING)
                .message("Your subscription is expiring")
                .status(NotificationStatus.FAILED)
                .build();

        entityManager.persist(pendingNotification);
        entityManager.persist(sentNotification);
        entityManager.persist(failedNotification);
        entityManager.flush();
    }

    @Test
    void testFindByCustomerId_ShouldReturnNotificationsForCustomer() {
        // When
        List<Notification> notifications = notificationRepository.findByCustomerId(customer1.getId());

        // Then
        assertThat(notifications).hasSize(2);
    }

    @Test
    void testFindByStatus_ShouldReturnNotificationsWithStatus() {
        // When
        List<Notification> pendingNotifications = notificationRepository.findByStatus(NotificationStatus.PENDING);

        // Then
        assertThat(pendingNotifications).hasSize(1);
        assertThat(pendingNotifications.get(0).getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void testFindPendingNotifications_ShouldReturnPendingNotifications() {
        // When
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications();

        // Then
        assertThat(pendingNotifications).hasSize(1);
        assertThat(pendingNotifications.get(0)).isEqualTo(pendingNotification);
    }

    @Test
    void testFindByNotificationTypeAndStatus_ShouldReturnMatchingNotifications() {
        // When
        List<Notification> notifications = notificationRepository.findByNotificationTypeAndStatus(
                NotificationType.REMINDER, NotificationStatus.PENDING);

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(pendingNotification);
    }

    @Test
    void testFindByCustomerIdAndStatus_ShouldReturnMatchingNotifications() {
        // When
        List<Notification> notifications = notificationRepository.findByCustomerIdAndStatus(
                customer1.getId(), NotificationStatus.SENT);

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(sentNotification);
    }

    @Test
    void testFindPendingNotificationsByCustomer_ShouldReturnPendingNotificationsForCustomer() {
        // When
        List<Notification> notifications = notificationRepository.findPendingNotificationsByCustomer(customer1.getId());

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(pendingNotification);
    }

    @Test
    void testFindBySubscriptionId_ShouldReturnNotificationsForSubscription() {
        // When
        List<Notification> notifications = notificationRepository.findBySubscriptionId(subscription.getId());

        // Then
        assertThat(notifications).hasSize(2);
    }

    @Test
    void testFindByNotificationType_ShouldReturnNotificationsOfType() {
        // When
        List<Notification> notifications = notificationRepository.findByNotificationType(
                NotificationType.REMINDER);

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getNotificationType()).isEqualTo(NotificationType.REMINDER);
    }

    @Test
    void testFindFailedNotifications_ShouldReturnFailedNotifications() {
        // When
        List<Notification> failedNotifications = notificationRepository.findFailedNotifications();

        // Then
        assertThat(failedNotifications).hasSize(1);
        assertThat(failedNotifications.get(0)).isEqualTo(failedNotification);
    }

    @Test
    void testFindSentNotificationsByDateRange_ShouldReturnNotificationsInRange() {
        // When
        LocalDateTime startDate = LocalDateTime.now().minusDays(5);
        LocalDateTime endDate = LocalDateTime.now();
        List<Notification> notifications = notificationRepository.findSentNotificationsByDateRange(startDate, endDate);

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(sentNotification);
    }

    @Test
    void testFindByBusinessId_ShouldReturnNotificationsForBusiness() {
        // When
        List<Notification> notifications = notificationRepository.findByBusinessId(business.getId());

        // Then
        assertThat(notifications).hasSize(3);
    }

    @Test
    void testFindPendingNotificationsByBusiness_ShouldReturnPendingNotificationsForBusiness() {
        // When
        List<Notification> notifications = notificationRepository.findPendingNotificationsByBusiness(business.getId());

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(pendingNotification);
    }

    @Test
    void testCountByCustomerId_ShouldReturnCorrectCount() {
        // When
        long count = notificationRepository.countByCustomerId(customer1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountPendingNotificationsByCustomer_ShouldReturnCorrectCount() {
        // When
        long count = notificationRepository.countPendingNotificationsByCustomer(customer1.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testCountFailedNotifications_ShouldReturnCorrectCount() {
        // When
        long count = notificationRepository.countFailedNotifications();

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testFindByCustomerIdAndNotificationType_ShouldReturnMatchingNotifications() {
        // When
        List<Notification> notifications = notificationRepository.findByCustomerIdAndNotificationType(
                customer1.getId(), NotificationType.REMINDER);

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0)).isEqualTo(pendingNotification);
    }

    @Test
    void testSaveNotification_ShouldPersistNotification() {
        // Given
        Notification newNotification = Notification.builder()
                .customer(customer2)
                .subscription(subscription)
                .notificationType(NotificationType.OVERDUE)
                .message("Your payment is overdue")
                .status(NotificationStatus.PENDING)
                .build();

        // When
        Notification saved = notificationRepository.saveAndFlush(newNotification);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(notificationRepository.findById(saved.getId())).isPresent();
    }
}
