package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PaymentPlanRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class PaymentPlanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    private Business business1;
    private Business business2;
    private PaymentPlan monthlyPlan;
    private PaymentPlan quarterlyPlan;
    private PaymentPlan inactivePlan;

    @BeforeEach
    void setUp() {
        // Create businesses
        business1 = Business.builder()
                .businessName("Gym 1")
                .ownerName("Owner 1")
                .contactEmail("owner1@gym.com")
                .contactPhone("09111111111")
                .isActive(true)
                .build();

        business2 = Business.builder()
                .businessName("Gym 2")
                .ownerName("Owner 2")
                .contactEmail("owner2@gym.com")
                .contactPhone("09222222222")
                .isActive(true)
                .build();

        entityManager.persist(business1);
        entityManager.persist(business2);

        // Create payment plans
        monthlyPlan = PaymentPlan.builder()
                .planName("Monthly Membership")
                .periodType(PeriodType.MONTHLY)
                .periodCount(1)
                .baseAmount(new BigDecimal("500000"))
                .discountPercentage(new BigDecimal("10.00"))
                .lateFeePerDay(new BigDecimal("10000"))
                .gracePeriodDays(3)
                .isActive(true)
                .business(business1)
                .build();

        quarterlyPlan = PaymentPlan.builder()
                .planName("Quarterly Membership")
                .periodType(PeriodType.QUARTERLY)
                .periodCount(1)
                .baseAmount(new BigDecimal("1400000"))
                .discountPercentage(new BigDecimal("5.00"))
                .lateFeePerDay(new BigDecimal("15000"))
                .gracePeriodDays(5)
                .isActive(true)
                .business(business1)
                .build();

        inactivePlan = PaymentPlan.builder()
                .planName("Old Plan")
                .periodType(PeriodType.MONTHLY)
                .periodCount(1)
                .baseAmount(new BigDecimal("400000"))
                .discountPercentage(BigDecimal.ZERO)
                .lateFeePerDay(new BigDecimal("8000"))
                .gracePeriodDays(2)
                .isActive(false)
                .business(business2)
                .build();

        entityManager.persist(monthlyPlan);
        entityManager.persist(quarterlyPlan);
        entityManager.persist(inactivePlan);
        entityManager.flush();
    }

    @Test
    void testFindByBusinessId_ShouldReturnPlansForBusiness() {
        // When
        List<PaymentPlan> plans = paymentPlanRepository.findByBusinessId(business1.getId());

        // Then
        assertThat(plans).hasSize(2);
        assertThat(plans).extracting(PaymentPlan::getPlanName)
                .containsExactlyInAnyOrder("Monthly Membership", "Quarterly Membership");
    }

    @Test
    void testFindByBusinessIdAndIsActiveTrue_ShouldReturnOnlyActivePlans() {
        // When
        List<PaymentPlan> activePlans = paymentPlanRepository.findByBusinessIdAndIsActiveTrue(business1.getId());

        // Then
        assertThat(activePlans).hasSize(2);
        assertThat(activePlans).allMatch(PaymentPlan::getIsActive);
    }

    @Test
    void testFindByPeriodType_ShouldReturnMatchingPlans() {
        // When
        List<PaymentPlan> monthlyPlans = paymentPlanRepository.findByPeriodType(PeriodType.MONTHLY);

        // Then
        assertThat(monthlyPlans).hasSize(2);
        assertThat(monthlyPlans).allMatch(plan -> plan.getPeriodType() == PeriodType.MONTHLY);
    }

    @Test
    void testFindByPeriodTypeAndIsActiveTrue_ShouldReturnActiveMonthlyPlans() {
        // When
        List<PaymentPlan> activePlans = paymentPlanRepository.findByPeriodTypeAndIsActiveTrue(PeriodType.MONTHLY);

        // Then
        assertThat(activePlans).hasSize(1);
        assertThat(activePlans.get(0).getPlanName()).isEqualTo("Monthly Membership");
    }

    @Test
    void testFindByBusinessIdAndPeriodType_ShouldReturnMatchingPlans() {
        // When
        List<PaymentPlan> plans = paymentPlanRepository.findByBusinessIdAndPeriodType(
                business1.getId(), PeriodType.QUARTERLY);

        // Then
        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getPlanName()).isEqualTo("Quarterly Membership");
    }

    @Test
    void testFindActiveByBusinessAndPeriodType_ShouldReturnMatchingPlans() {
        // When
        List<PaymentPlan> plans = paymentPlanRepository.findActiveByBusinessAndPeriodType(
                business1.getId(), PeriodType.MONTHLY);

        // Then
        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getPlanName()).isEqualTo("Monthly Membership");
    }

    @Test
    void testFindByIsActiveTrue_ShouldReturnAllActivePlans() {
        // When
        List<PaymentPlan> activePlans = paymentPlanRepository.findByIsActiveTrue();

        // Then
        assertThat(activePlans).hasSize(2);
        assertThat(activePlans).allMatch(PaymentPlan::getIsActive);
    }

    @Test
    void testFindByPlanNameContainingIgnoreCase_ShouldReturnMatchingPlans() {
        // When
        List<PaymentPlan> found = paymentPlanRepository.findByPlanNameContainingIgnoreCase("membership");

        // Then
        assertThat(found).hasSize(2);
    }

    @Test
    void testCountByBusinessId_ShouldReturnCorrectCount() {
        // When
        long count = paymentPlanRepository.countByBusinessId(business1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByBusinessIdAndIsActiveTrue_ShouldReturnCorrectCount() {
        // When
        long count = paymentPlanRepository.countByBusinessIdAndIsActiveTrue(business1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindPlansWithDiscountsByBusiness_ShouldReturnPlansWithDiscounts() {
        // When
        List<PaymentPlan> plansWithDiscounts = paymentPlanRepository.findPlansWithDiscountsByBusiness(business1.getId());

        // Then
        assertThat(plansWithDiscounts).hasSize(2);
        assertThat(plansWithDiscounts).allMatch(plan -> 
                plan.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testSavePaymentPlan_ShouldPersistPlan() {
        // Given
        PaymentPlan newPlan = PaymentPlan.builder()
                .planName("Weekly Plan")
                .periodType(PeriodType.WEEKLY)
                .periodCount(1)
                .baseAmount(new BigDecimal("150000"))
                .discountPercentage(BigDecimal.ZERO)
                .lateFeePerDay(new BigDecimal("5000"))
                .gracePeriodDays(1)
                .isActive(true)
                .business(business1)
                .build();

        // When
        PaymentPlan saved = paymentPlanRepository.saveAndFlush(newPlan);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(paymentPlanRepository.findById(saved.getId())).isPresent();
    }
}
