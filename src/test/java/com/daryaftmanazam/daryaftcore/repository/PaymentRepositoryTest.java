package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PaymentRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private Business business;
    private Customer customer;
    private PaymentPlan paymentPlan;
    private Subscription subscription;
    private Payment completedPayment;
    private Payment pendingPayment;
    private Payment failedPayment;

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

        // Create customer
        customer = Customer.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .phoneNumber("09123456789")
                .email("ali@example.com")
                .customerType(CustomerType.REGULAR)
                .isActive(true)
                .business(business)
                .build();
        entityManager.persist(customer);

        // Create subscription
        subscription = Subscription.builder()
                .customer(customer)
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(new BigDecimal("1350000"))
                .nextPaymentDate(LocalDate.now().plusDays(30))
                .build();
        entityManager.persist(subscription);

        // Create payments
        completedPayment = Payment.builder()
                .subscription(subscription)
                .amount(new BigDecimal("450000"))
                .paymentDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDate.now().minusDays(10))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("Card")
                .transactionId("TXN001")
                .lateFee(BigDecimal.ZERO)
                .build();

        pendingPayment = Payment.builder()
                .subscription(subscription)
                .amount(new BigDecimal("450000"))
                .dueDate(LocalDate.now().plusDays(5))
                .status(PaymentStatus.PENDING)
                .paymentMethod("Cash")
                .lateFee(BigDecimal.ZERO)
                .build();

        failedPayment = Payment.builder()
                .subscription(subscription)
                .amount(new BigDecimal("450000"))
                .paymentDate(LocalDateTime.now().minusDays(2))
                .dueDate(LocalDate.now().minusDays(7))
                .status(PaymentStatus.FAILED)
                .paymentMethod("Card")
                .transactionId("TXN002")
                .lateFee(new BigDecimal("30000"))
                .notes("Insufficient funds")
                .build();

        entityManager.persist(completedPayment);
        entityManager.persist(pendingPayment);
        entityManager.persist(failedPayment);
        entityManager.flush();
    }

    @Test
    void testFindBySubscriptionId_ShouldReturnPaymentsForSubscription() {
        // When
        List<Payment> payments = paymentRepository.findBySubscriptionId(subscription.getId());

        // Then
        assertThat(payments).hasSize(3);
    }

    @Test
    void testFindByCustomerId_ShouldReturnPaymentsForCustomer() {
        // When
        List<Payment> payments = paymentRepository.findByCustomerId(customer.getId());

        // Then
        assertThat(payments).hasSize(3);
    }

    @Test
    void testFindByStatus_ShouldReturnPaymentsWithStatus() {
        // When
        List<Payment> completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED);

        // Then
        assertThat(completedPayments).hasSize(1);
        assertThat(completedPayments.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void testFindPendingPayments_ShouldReturnPendingPayments() {
        // When
        List<Payment> pendingPayments = paymentRepository.findPendingPayments();

        // Then
        assertThat(pendingPayments).hasSize(1);
        assertThat(pendingPayments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void testFindByDateRange_ShouldReturnPaymentsInRange() {
        // When
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();
        List<Payment> payments = paymentRepository.findByDateRange(startDate, endDate);

        // Then
        assertThat(payments).hasSize(2); // completed and failed payments
    }

    @Test
    void testCalculateTotalPaymentsByCustomer_ShouldReturnCorrectTotal() {
        // When
        BigDecimal total = paymentRepository.calculateTotalPaymentsByCustomer(customer.getId());

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("450000"));
    }

    @Test
    void testCalculateTotalPaymentsBySubscription_ShouldReturnCorrectTotal() {
        // When
        BigDecimal total = paymentRepository.calculateTotalPaymentsBySubscription(subscription.getId());

        // Then
        assertThat(total).isEqualByComparingTo(new BigDecimal("450000"));
    }

    @Test
    void testFindByTransactionId_ShouldReturnPayment() {
        // When
        Optional<Payment> found = paymentRepository.findByTransactionId("TXN001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void testFindBySubscriptionIdAndStatus_ShouldReturnMatchingPayments() {
        // When
        List<Payment> payments = paymentRepository.findBySubscriptionIdAndStatus(
                subscription.getId(), PaymentStatus.PENDING);

        // Then
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0)).isEqualTo(pendingPayment);
    }

    @Test
    void testFindPendingPaymentsBySubscription_ShouldReturnPendingPayments() {
        // When
        List<Payment> pendingPayments = paymentRepository.findPendingPaymentsBySubscription(subscription.getId());

        // Then
        assertThat(pendingPayments).hasSize(1);
        assertThat(pendingPayments.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void testFindFailedPaymentsByCustomer_ShouldReturnFailedPayments() {
        // When
        List<Payment> failedPayments = paymentRepository.findFailedPaymentsByCustomer(customer.getId());

        // Then
        assertThat(failedPayments).hasSize(1);
        assertThat(failedPayments.get(0).getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void testCountBySubscriptionId_ShouldReturnCorrectCount() {
        // When
        long count = paymentRepository.countBySubscriptionId(subscription.getId());

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testCountCompletedPaymentsByCustomer_ShouldReturnCorrectCount() {
        // When
        long count = paymentRepository.countCompletedPaymentsByCustomer(customer.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testFindByBusinessId_ShouldReturnPaymentsForBusiness() {
        // When
        List<Payment> payments = paymentRepository.findByBusinessId(business.getId());

        // Then
        assertThat(payments).hasSize(3);
    }

    @Test
    void testCalculateTotalRevenueByBusiness_ShouldReturnCorrectTotal() {
        // When
        BigDecimal revenue = paymentRepository.calculateTotalRevenueByBusiness(business.getId());

        // Then
        assertThat(revenue).isEqualByComparingTo(new BigDecimal("450000"));
    }

    @Test
    void testFindPaymentsWithLateFees_ShouldReturnPaymentsWithFees() {
        // When
        List<Payment> paymentsWithFees = paymentRepository.findPaymentsWithLateFees();

        // Then
        assertThat(paymentsWithFees).hasSize(1);
        assertThat(paymentsWithFees.get(0)).isEqualTo(failedPayment);
        assertThat(paymentsWithFees.get(0).getLateFee()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void testFindOverduePayments_ShouldReturnOverduePayments() {
        // Given - create an overdue payment
        Payment overduePayment = Payment.builder()
                .subscription(subscription)
                .amount(new BigDecimal("450000"))
                .dueDate(LocalDate.now().minusDays(10))
                .status(PaymentStatus.PENDING)
                .paymentMethod("Cash")
                .lateFee(BigDecimal.ZERO)
                .build();
        entityManager.persist(overduePayment);
        entityManager.flush();

        // When
        List<Payment> overduePayments = paymentRepository.findOverduePayments(LocalDate.now());

        // Then
        assertThat(overduePayments).hasSize(1);
        assertThat(overduePayments.get(0)).isEqualTo(overduePayment);
    }

    @Test
    void testSavePayment_ShouldPersistPayment() {
        // Given
        Payment newPayment = Payment.builder()
                .subscription(subscription)
                .amount(new BigDecimal("450000"))
                .paymentDate(LocalDateTime.now())
                .dueDate(LocalDate.now())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("Online")
                .transactionId("TXN003")
                .lateFee(BigDecimal.ZERO)
                .build();

        // When
        Payment saved = paymentRepository.saveAndFlush(newPayment);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(paymentRepository.findById(saved.getId())).isPresent();
    }
}
