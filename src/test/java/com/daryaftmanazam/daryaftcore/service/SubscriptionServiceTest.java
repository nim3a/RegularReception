package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.dto.request.SubscriptionRequest;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.exception.CustomerNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.PaymentPlanNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.SubscriptionNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentInterval;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentPlanRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionService
 * Tests business logic for subscriptions using mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription Service Unit Tests")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentPlanRepository paymentPlanRepository;

    @Mock
    private PaymentPlanService paymentPlanService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Customer testCustomer;
    private PaymentPlan testPlan;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFirstName("علی");
        testCustomer.setLastName("محمدی");
        testCustomer.setPhoneNumber("09121234567");
        testCustomer.setBusinessId(1L);

        testPlan = new PaymentPlan();
        testPlan.setId(1L);
        testPlan.setName("ماهانه استاندارد");
        testPlan.setAmount(BigDecimal.valueOf(500000));
        testPlan.setPaymentInterval(PaymentInterval.MONTHLY);
        testPlan.setLateFeePercentage(BigDecimal.valueOf(2));
        testPlan.setBusinessId(1L);

        testSubscription = new Subscription();
        testSubscription.setId(1L);
        testSubscription.setCustomer(testCustomer);
        testSubscription.setPaymentPlan(testPlan);
        testSubscription.setBusinessId(1L);
        testSubscription.setStartDate(LocalDate.now());
        testSubscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);
        testSubscription.setTotalAmount(BigDecimal.valueOf(500000));
    }

    @Test
    @DisplayName("Should calculate next payment date correctly for monthly subscription")
    void testCalculateNextPaymentDate_Monthly() {
        // Given
        LocalDate currentDate = LocalDate.of(2024, 1, 15);
        PaymentInterval interval = PaymentInterval.MONTHLY;

        // When
        LocalDate nextDate = currentDate.plusMonths(1);

        // Then
        assertThat(nextDate).isEqualTo(LocalDate.of(2024, 2, 15));
    }

    @Test
    @DisplayName("Should calculate next payment date correctly for yearly subscription")
    void testCalculateNextPaymentDate_Yearly() {
        // Given
        LocalDate currentDate = LocalDate.of(2024, 1, 15);
        PaymentInterval interval = PaymentInterval.YEARLY;

        // When
        LocalDate nextDate = currentDate.plusYears(1);

        // Then
        assertThat(nextDate).isEqualTo(LocalDate.of(2025, 1, 15));
    }

    @Test
    @DisplayName("Should calculate overdue amount with late fee correctly")
    void testCalculateOverdueAmount_WithLateFee() {
        // Given - Subscription is 10 days overdue
        testSubscription.setNextPaymentDate(LocalDate.now().minusDays(10));
        testSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        
        long daysOverdue = ChronoUnit.DAYS.between(
                testSubscription.getNextPaymentDate(), 
                LocalDate.now()
        );
        
        // Late fee: 2% per day * 10 days = 20%
        // Total: 500000 + (500000 * 0.20) = 600000
        BigDecimal baseAmount = testSubscription.getTotalAmount();
        BigDecimal lateFeeRate = testPlan.getLateFeePercentage()
                .divide(BigDecimal.valueOf(100))
                .multiply(BigDecimal.valueOf(daysOverdue));
        BigDecimal expectedAmount = baseAmount.add(
                baseAmount.multiply(lateFeeRate)
        );

        // Then
        assertThat(expectedAmount).isEqualByComparingTo(BigDecimal.valueOf(600000));
        assertThat(daysOverdue).isEqualTo(10);
    }

    @Test
    @DisplayName("Should calculate zero late fee when not overdue")
    void testCalculateOverdueAmount_NotOverdue() {
        // Given - Subscription is not overdue (payment due in future)
        testSubscription.setNextPaymentDate(LocalDate.now().plusDays(5));
        testSubscription.setTotalAmount(BigDecimal.valueOf(500000));

        // When calculating overdue amount
        long daysOverdue = ChronoUnit.DAYS.between(
                testSubscription.getNextPaymentDate(), 
                LocalDate.now()
        );

        // Then - No late fee should be applied
        assertThat(daysOverdue).isLessThan(0);
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void testCreateSubscription_CustomerNotFound() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(999L);
        request.setPaymentPlanId(1L);
        request.setStartDate(LocalDate.now());

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.createSubscription(request))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when payment plan not found")
    void testCreateSubscription_PaymentPlanNotFound() {
        // Given
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(1L);
        request.setPaymentPlanId(999L);
        request.setStartDate(LocalDate.now());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(paymentPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subscriptionService.createSubscription(request))
                .isInstanceOf(PaymentPlanNotFoundException.class);
    }

    @Test
    @DisplayName("Should determine if subscription is overdue correctly")
    void testIsSubscriptionOverdue() {
        // Given - Overdue subscription
        testSubscription.setNextPaymentDate(LocalDate.now().minusDays(1));

        // When
        boolean isOverdue = testSubscription.getNextPaymentDate().isBefore(LocalDate.now());

        // Then
        assertThat(isOverdue).isTrue();
    }

    @Test
    @DisplayName("Should calculate days until next payment correctly")
    void testCalculateDaysUntilNextPayment() {
        // Given
        testSubscription.setNextPaymentDate(LocalDate.now().plusDays(15));

        // When
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), testSubscription.getNextPaymentDate());

        // Then
        assertThat(daysUntil).isEqualTo(15);
    }

    @Test
    @DisplayName("Should calculate subscription duration in months")
    void testCalculateSubscriptionDuration() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 1);

        // When
        long months = ChronoUnit.MONTHS.between(startDate, endDate);

        // Then
        assertThat(months).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle leap year in date calculations")
    void testCalculateNextPaymentDate_LeapYear() {
        // Given
        LocalDate leapYearDate = LocalDate.of(2024, 2, 29);

        // When
        LocalDate nextYear = leapYearDate.plusYears(1);

        // Then - Should handle Feb 29 -> Feb 28 in non-leap year
        assertThat(nextYear).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Should calculate prorated amount correctly")
    void testCalculateProratedAmount() {
        // Given - Customer starts mid-month (15 days into 30-day month)
        BigDecimal monthlyAmount = BigDecimal.valueOf(600000);
        int daysUsed = 15;
        int daysInMonth = 30;

        // When
        BigDecimal proratedAmount = monthlyAmount
                .multiply(BigDecimal.valueOf(daysUsed))
                .divide(BigDecimal.valueOf(daysInMonth), 2, BigDecimal.ROUND_HALF_UP);

        // Then - Should charge half the monthly amount
        assertThat(proratedAmount).isEqualByComparingTo(BigDecimal.valueOf(300000));
    }
}
