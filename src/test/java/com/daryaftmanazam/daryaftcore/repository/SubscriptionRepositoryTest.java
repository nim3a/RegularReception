package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SubscriptionRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class SubscriptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private Business business;
    private Customer customer1;
    private Customer customer2;
    private PaymentPlan paymentPlan;
    private Subscription activeSubscription;
    private Subscription overdueSubscription;
    private Subscription expiringSubscription;

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
                .discountPercentage(new BigDecimal("10.00"))
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

        // Create subscriptions
        activeSubscription = Subscription.builder()
                .customer(customer1)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("450000"))
                .discountApplied(new BigDecimal("50000"))
                .nextPaymentDate(LocalDate.now().plusDays(15))
                .build();

        overdueSubscription = Subscription.builder()
                .customer(customer1)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().plusMonths(1))
                .status(SubscriptionStatus.OVERDUE)
                .totalAmount(new BigDecimal("450000"))
                .nextPaymentDate(LocalDate.now().minusDays(5))
                .build();

        expiringSubscription = Subscription.builder()
                .customer(customer2)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusDays(5))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("450000"))
                .nextPaymentDate(LocalDate.now().plusDays(3))
                .build();

        entityManager.persist(activeSubscription);
        entityManager.persist(overdueSubscription);
        entityManager.persist(expiringSubscription);
        entityManager.flush();
    }

    @Test
    void testFindByCustomerId_ShouldReturnSubscriptionsForCustomer() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByCustomerId(customer1.getId());

        // Then
        assertThat(subscriptions).hasSize(2);
    }

    @Test
    void testFindByStatus_ShouldReturnSubscriptionsWithStatus() {
        // When
        List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);

        // Then
        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).allMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE);
    }

    @Test
    void testFindActiveSubscriptions_ShouldReturnActiveSubscriptions() {
        // When
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptions();

        // Then
        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).allMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE);
    }

    @Test
    void testFindOverdueSubscriptions_ShouldReturnOverdueSubscriptions() {
        // When
        List<Subscription> overdueSubscriptions = subscriptionRepository.findOverdueSubscriptions(LocalDate.now());

        // Then
        assertThat(overdueSubscriptions).hasSize(1);
        assertThat(overdueSubscriptions.get(0).getStatus()).isEqualTo(SubscriptionStatus.OVERDUE);
    }

    @Test
    void testFindExpiringSoon_ShouldReturnExpiringSubscriptions() {
        // When
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        List<Subscription> expiringSubscriptions = subscriptionRepository.findExpiringSoon(startDate, endDate);

        // Then
        assertThat(expiringSubscriptions).hasSize(1);
        assertThat(expiringSubscriptions.get(0).getCustomer().getId()).isEqualTo(customer2.getId());
    }

    @Test
    void testFindByBusinessId_ShouldReturnSubscriptionsForBusiness() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByBusinessId(business.getId());

        // Then
        assertThat(subscriptions).hasSize(3);
    }

    @Test
    void testFindActiveSubscriptionsByBusinessId_ShouldReturnActiveSubscriptionsForBusiness() {
        // When
        List<Subscription> activeSubscriptions = 
                subscriptionRepository.findActiveSubscriptionsByBusinessId(business.getId());

        // Then
        assertThat(activeSubscriptions).hasSize(2);
        assertThat(activeSubscriptions).allMatch(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE);
    }

    @Test
    void testFindByCustomerIdAndStatus_ShouldReturnMatchingSubscriptions() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByCustomerIdAndStatus(
                customer1.getId(), SubscriptionStatus.ACTIVE);

        // Then
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions.get(0)).isEqualTo(activeSubscription);
    }

    @Test
    void testFindByPaymentPlanId_ShouldReturnSubscriptionsForPlan() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findByPaymentPlanId(paymentPlan.getId());

        // Then
        assertThat(subscriptions).hasSize(3);
    }

    @Test
    void testCountByCustomerId_ShouldReturnCorrectCount() {
        // When
        long count = subscriptionRepository.countByCustomerId(customer1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountActiveSubscriptionsByCustomerId_ShouldReturnCorrectCount() {
        // When
        long count = subscriptionRepository.countActiveSubscriptionsByCustomerId(customer1.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testCountByBusinessId_ShouldReturnCorrectCount() {
        // When
        long count = subscriptionRepository.countByBusinessId(business.getId());

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testFindSubscriptionsWithPaymentDueBefore_ShouldReturnMatchingSubscriptions() {
        // When
        List<Subscription> subscriptions = subscriptionRepository.findSubscriptionsWithPaymentDueBefore(
                LocalDate.now().plusDays(10));

        // Then
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions.get(0).getCustomer().getId()).isEqualTo(customer2.getId());
    }

    @Test
    void testFindExpiredButNotMarked_ShouldReturnExpiredSubscriptions() {
        // Given - create an expired subscription that's still marked as active
        Subscription expiredSub = Subscription.builder()
                .customer(customer2)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now().minusMonths(2))
                .endDate(LocalDate.now().minusDays(5))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("450000"))
                .build();
        entityManager.persist(expiredSub);
        entityManager.flush();

        // When
        List<Subscription> expired = subscriptionRepository.findExpiredButNotMarked(LocalDate.now());

        // Then
        assertThat(expired).hasSize(1);
        assertThat(expired.get(0)).isEqualTo(expiredSub);
    }

    @Test
    void testSaveSubscription_ShouldPersistSubscription() {
        // Given
        Subscription newSubscription = Subscription.builder()
                .customer(customer2)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("450000"))
                .nextPaymentDate(LocalDate.now().plusDays(30))
                .build();

        // When
        Subscription saved = subscriptionRepository.saveAndFlush(newSubscription);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(subscriptionRepository.findById(saved.getId())).isPresent();
    }
}
