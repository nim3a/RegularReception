package com.daryaftmanazam.daryaftcore.scheduler;

import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SubscriptionScheduler.
 * Tests scheduled methods by calling them directly.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubscriptionSchedulerTest {

    @Autowired
    private SubscriptionScheduler subscriptionScheduler;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    private Customer testCustomer;
    private PaymentPlan testPaymentPlan;

    @BeforeEach
    void setUp() {
        // Clear data
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
    void testCheckOverdueSubscriptions_ShouldMarkAsOverdue() {
        // Given: ACTIVE subscription with past payment date
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .nextPaymentDate(LocalDate.now().minusDays(2))  // 2 days overdue
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        subscriptionScheduler.checkOverdueSubscriptions();

        // Then: Status should be changed to OVERDUE
        Subscription updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.OVERDUE);
    }

    @Test
    void testCheckOverdueSubscriptions_ShouldNotMarkFuturePayments() {
        // Given: ACTIVE subscription with future payment date
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().plusDays(5))  // Future payment
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        subscriptionScheduler.checkOverdueSubscriptions();

        // Then: Status should remain ACTIVE
        Subscription updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void testAutoDeactivateExpiredSubscriptions_ShouldExpireOldOverdue() {
        // Given: OVERDUE subscription older than 30 days
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.OVERDUE)
                .startDate(LocalDate.now().minusDays(60))
                .endDate(LocalDate.now().minusDays(30))
                .nextPaymentDate(LocalDate.now().minusDays(35))  // 35 days overdue
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        subscriptionScheduler.autoDeactivateExpiredSubscriptions();

        // Then: Status should be changed to EXPIRED
        Subscription updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
    }

    @Test
    void testAutoDeactivateExpiredSubscriptions_ShouldNotExpireRecentOverdue() {
        // Given: OVERDUE subscription less than 30 days old
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.OVERDUE)
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusDays(10))
                .nextPaymentDate(LocalDate.now().minusDays(10))  // 10 days overdue
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        subscriptionScheduler.autoDeactivateExpiredSubscriptions();

        // Then: Status should remain OVERDUE
        Subscription updatedSubscription = subscriptionRepository.findById(subscription.getId()).orElseThrow();
        assertThat(updatedSubscription.getStatus()).isEqualTo(SubscriptionStatus.OVERDUE);
    }

    @Test
    void testAutoDeactivateExpiredSubscriptions_ShouldDeactivateCustomer() {
        // Given: OVERDUE subscription older than 30 days (only subscription for customer)
        Subscription subscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.OVERDUE)
                .startDate(LocalDate.now().minusDays(60))
                .endDate(LocalDate.now().minusDays(30))
                .nextPaymentDate(LocalDate.now().minusDays(35))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // When: Run the scheduled task
        subscriptionScheduler.autoDeactivateExpiredSubscriptions();

        // Then: Customer should be deactivated
        Customer updatedCustomer = customerRepository.findById(testCustomer.getId()).orElseThrow();
        assertThat(updatedCustomer.getIsActive()).isFalse();
    }

    @Test
    void testAutoDeactivateExpiredSubscriptions_ShouldNotDeactivateCustomerWithActiveSubscriptions() {
        // Given: Customer with one expired and one active subscription
        Subscription expiredSubscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.OVERDUE)
                .startDate(LocalDate.now().minusDays(60))
                .endDate(LocalDate.now().minusDays(30))
                .nextPaymentDate(LocalDate.now().minusDays(35))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        expiredSubscription = subscriptionRepository.save(expiredSubscription);

        Subscription activeSubscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().plusDays(10))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        activeSubscription = subscriptionRepository.save(activeSubscription);

        // When: Run the scheduled task
        subscriptionScheduler.autoDeactivateExpiredSubscriptions();

        // Then: Customer should remain active
        Customer updatedCustomer = customerRepository.findById(testCustomer.getId()).orElseThrow();
        assertThat(updatedCustomer.getIsActive()).isTrue();
    }

    @Test
    void testCheckOverdueSubscriptions_ShouldHandleMultipleSubscriptions() {
        // Given: Multiple subscriptions with different dates
        Subscription overdueSubscription1 = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .nextPaymentDate(LocalDate.now().minusDays(2))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        overdueSubscription1 = subscriptionRepository.save(overdueSubscription1);

        Subscription overdueSubscription2 = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .nextPaymentDate(LocalDate.now().minusDays(5))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        overdueSubscription2 = subscriptionRepository.save(overdueSubscription2);

        Subscription activeSubscription = Subscription.builder()
                .customer(testCustomer)
                .paymentPlan(testPaymentPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .nextPaymentDate(LocalDate.now().plusDays(10))
                .totalAmount(new BigDecimal("100.00"))
                .build();
        activeSubscription = subscriptionRepository.save(activeSubscription);

        // When: Run the scheduled task
        subscriptionScheduler.checkOverdueSubscriptions();

        // Then: Only overdue subscriptions should be marked
        List<Subscription> overdueSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.OVERDUE);
        assertThat(overdueSubscriptions).hasSize(2);
        assertThat(overdueSubscriptions).extracting(Subscription::getId)
                .containsExactlyInAnyOrder(overdueSubscription1.getId(), overdueSubscription2.getId());

        // Active subscription should remain active
        Subscription updatedActive = subscriptionRepository.findById(activeSubscription.getId()).orElseThrow();
        assertThat(updatedActive.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }
}
