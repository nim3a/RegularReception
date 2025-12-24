package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentInterval;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for SubscriptionRepository
 * Tests database queries and custom repository methods
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Subscription Repository Integration Tests")
class SubscriptionRepositoryIntegrationTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Business testBusiness;
    private Customer testCustomer;
    private PaymentPlan testPlan;

    @BeforeEach
    void setUp() {
        // Create and persist test business
        testBusiness = new Business();
        testBusiness.setName("Test Business");
        testBusiness.setOwnerName("Test Owner");
        testBusiness.setPhoneNumber("09121111111");
        testBusiness.setAddress("Test Address");
        entityManager.persist(testBusiness);

        // Create and persist test customer
        testCustomer = new Customer();
        testCustomer.setFirstName("علی");
        testCustomer.setLastName("محمدی");
        testCustomer.setPhoneNumber("09123456789");
        testCustomer.setBusinessId(testBusiness.getId());
        entityManager.persist(testCustomer);

        // Create and persist test payment plan
        testPlan = new PaymentPlan();
        testPlan.setName("ماهانه");
        testPlan.setDescription("پلن ماهانه");
        testPlan.setAmount(BigDecimal.valueOf(500000));
        testPlan.setPaymentInterval(PaymentInterval.MONTHLY);
        testPlan.setBusinessId(testBusiness.getId());
        entityManager.persist(testPlan);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find subscriptions expiring soon")
    void testFindExpiringSoon() {
        // Given - Create subscription expiring in 3 days
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        
        Subscription expiringSoon = new Subscription();
        expiringSoon.setCustomer(testCustomer);
        expiringSoon.setPaymentPlan(testPlan);
        expiringSoon.setBusinessId(testBusiness.getId());
        expiringSoon.setStartDate(LocalDate.now());
        expiringSoon.setNextPaymentDate(threeDaysLater);
        expiringSoon.setStatus(SubscriptionStatus.ACTIVE);
        expiringSoon.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(expiringSoon);

        // Create subscription expiring in 10 days (should not be included)
        Subscription expiringLater = new Subscription();
        expiringLater.setCustomer(testCustomer);
        expiringLater.setPaymentPlan(testPlan);
        expiringLater.setBusinessId(testBusiness.getId());
        expiringLater.setStartDate(LocalDate.now());
        expiringLater.setNextPaymentDate(LocalDate.now().plusDays(10));
        expiringLater.setStatus(SubscriptionStatus.ACTIVE);
        expiringLater.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(expiringLater);

        entityManager.flush();

        // When - Query for subscriptions expiring within 7 days
        List<Subscription> result = subscriptionRepository
                .findByBusinessIdAndNextPaymentDateBetweenAndStatus(
                        testBusiness.getId(),
                        LocalDate.now(),
                        LocalDate.now().plusDays(7),
                        SubscriptionStatus.ACTIVE
                );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNextPaymentDate()).isEqualTo(threeDaysLater);
    }

    @Test
    @DisplayName("Should find overdue subscriptions")
    void testFindOverdueSubscriptions() {
        // Given - Create overdue subscription
        Subscription overdueSubscription = new Subscription();
        overdueSubscription.setCustomer(testCustomer);
        overdueSubscription.setPaymentPlan(testPlan);
        overdueSubscription.setBusinessId(testBusiness.getId());
        overdueSubscription.setStartDate(LocalDate.now().minusMonths(2));
        overdueSubscription.setNextPaymentDate(LocalDate.now().minusDays(5));
        overdueSubscription.setStatus(SubscriptionStatus.OVERDUE);
        overdueSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(overdueSubscription);

        // Create active subscription (not overdue)
        Subscription activeSubscription = new Subscription();
        activeSubscription.setCustomer(testCustomer);
        activeSubscription.setPaymentPlan(testPlan);
        activeSubscription.setBusinessId(testBusiness.getId());
        activeSubscription.setStartDate(LocalDate.now());
        activeSubscription.setNextPaymentDate(LocalDate.now().plusDays(10));
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(activeSubscription);

        entityManager.flush();

        // When
        List<Subscription> result = subscriptionRepository
                .findByBusinessIdAndStatus(testBusiness.getId(), SubscriptionStatus.OVERDUE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SubscriptionStatus.OVERDUE);
        assertThat(result.get(0).getNextPaymentDate()).isBefore(LocalDate.now());
    }

    @Test
    @DisplayName("Should find subscriptions by customer")
    void testFindByCustomer() {
        // Given - Create multiple subscriptions for customer
        for (int i = 0; i < 3; i++) {
            Subscription subscription = new Subscription();
            subscription.setCustomer(testCustomer);
            subscription.setPaymentPlan(testPlan);
            subscription.setBusinessId(testBusiness.getId());
            subscription.setStartDate(LocalDate.now().minusMonths(i));
            subscription.setNextPaymentDate(LocalDate.now().plusMonths(1 - i));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setTotalAmount(BigDecimal.valueOf(500000));
            entityManager.persist(subscription);
        }
        entityManager.flush();

        // When
        List<Subscription> result = subscriptionRepository
                .findByCustomerId(testCustomer.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(s -> s.getCustomer().getId().equals(testCustomer.getId()));
    }

    @Test
    @DisplayName("Should enforce multi-tenant isolation by business ID")
    void testMultiTenantIsolation() {
        // Given - Create another business with its own subscription
        Business otherBusiness = new Business();
        otherBusiness.setName("Other Business");
        otherBusiness.setOwnerName("Other Owner");
        otherBusiness.setPhoneNumber("09129999999");
        otherBusiness.setAddress("Other Address");
        entityManager.persist(otherBusiness);

        Customer otherCustomer = new Customer();
        otherCustomer.setFirstName("محمد");
        otherCustomer.setLastName("رضایی");
        otherCustomer.setPhoneNumber("09129876543");
        otherCustomer.setBusinessId(otherBusiness.getId());
        entityManager.persist(otherCustomer);

        Subscription subscription1 = new Subscription();
        subscription1.setCustomer(testCustomer);
        subscription1.setPaymentPlan(testPlan);
        subscription1.setBusinessId(testBusiness.getId());
        subscription1.setStartDate(LocalDate.now());
        subscription1.setNextPaymentDate(LocalDate.now().plusMonths(1));
        subscription1.setStatus(SubscriptionStatus.ACTIVE);
        subscription1.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(subscription1);

        Subscription subscription2 = new Subscription();
        subscription2.setCustomer(otherCustomer);
        subscription2.setPaymentPlan(testPlan);
        subscription2.setBusinessId(otherBusiness.getId());
        subscription2.setStartDate(LocalDate.now());
        subscription2.setNextPaymentDate(LocalDate.now().plusMonths(1));
        subscription2.setStatus(SubscriptionStatus.ACTIVE);
        subscription2.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(subscription2);

        entityManager.flush();

        // When - Query for business 1 subscriptions
        List<Subscription> business1Subs = subscriptionRepository
                .findByBusinessId(testBusiness.getId());

        // Then - Should only get business 1 subscription
        assertThat(business1Subs).hasSize(1);
        assertThat(business1Subs.get(0).getBusinessId()).isEqualTo(testBusiness.getId());
    }

    @Test
    @DisplayName("Should find subscriptions by status")
    void testFindByStatus() {
        // Given - Create subscriptions with different statuses
        Subscription activeSubscription = new Subscription();
        activeSubscription.setCustomer(testCustomer);
        activeSubscription.setPaymentPlan(testPlan);
        activeSubscription.setBusinessId(testBusiness.getId());
        activeSubscription.setStartDate(LocalDate.now());
        activeSubscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(activeSubscription);

        Subscription cancelledSubscription = new Subscription();
        cancelledSubscription.setCustomer(testCustomer);
        cancelledSubscription.setPaymentPlan(testPlan);
        cancelledSubscription.setBusinessId(testBusiness.getId());
        cancelledSubscription.setStartDate(LocalDate.now().minusMonths(1));
        cancelledSubscription.setNextPaymentDate(LocalDate.now());
        cancelledSubscription.setStatus(SubscriptionStatus.CANCELLED);
        cancelledSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        entityManager.persist(cancelledSubscription);

        entityManager.flush();

        // When
        List<Subscription> activeResults = subscriptionRepository
                .findByBusinessIdAndStatus(testBusiness.getId(), SubscriptionStatus.ACTIVE);
        List<Subscription> cancelledResults = subscriptionRepository
                .findByBusinessIdAndStatus(testBusiness.getId(), SubscriptionStatus.CANCELLED);

        // Then
        assertThat(activeResults).hasSize(1);
        assertThat(activeResults.get(0).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(cancelledResults).hasSize(1);
        assertThat(cancelledResults.get(0).getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should support pagination")
    void testPagination() {
        // Given - Create 15 subscriptions
        for (int i = 0; i < 15; i++) {
            Subscription subscription = new Subscription();
            subscription.setCustomer(testCustomer);
            subscription.setPaymentPlan(testPlan);
            subscription.setBusinessId(testBusiness.getId());
            subscription.setStartDate(LocalDate.now().minusDays(i));
            subscription.setNextPaymentDate(LocalDate.now().plusMonths(1).minusDays(i));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setTotalAmount(BigDecimal.valueOf(500000));
            entityManager.persist(subscription);
        }
        entityManager.flush();

        // When - Get first page (10 items)
        Page<Subscription> page = subscriptionRepository
                .findByBusinessId(testBusiness.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Should count subscriptions by business and status")
    void testCountByBusinessAndStatus() {
        // Given
        for (int i = 0; i < 5; i++) {
            Subscription subscription = new Subscription();
            subscription.setCustomer(testCustomer);
            subscription.setPaymentPlan(testPlan);
            subscription.setBusinessId(testBusiness.getId());
            subscription.setStartDate(LocalDate.now());
            subscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
            subscription.setStatus(i < 3 ? SubscriptionStatus.ACTIVE : SubscriptionStatus.CANCELLED);
            subscription.setTotalAmount(BigDecimal.valueOf(500000));
            entityManager.persist(subscription);
        }
        entityManager.flush();

        // When
        long activeCount = subscriptionRepository
                .countByBusinessIdAndStatus(testBusiness.getId(), SubscriptionStatus.ACTIVE);
        long cancelledCount = subscriptionRepository
                .countByBusinessIdAndStatus(testBusiness.getId(), SubscriptionStatus.CANCELLED);

        // Then
        assertThat(activeCount).isEqualTo(3);
        assertThat(cancelledCount).isEqualTo(2);
    }
}
