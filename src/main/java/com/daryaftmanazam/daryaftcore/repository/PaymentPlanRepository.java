package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PaymentPlan entity operations.
 */
@Repository
public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {

    /**
     * Find payment plans by business ID.
     *
     * @param businessId the business ID
     * @return List of payment plans for the specified business
     */
    List<PaymentPlan> findByBusinessId(Long businessId);

    /**
     * Find active payment plans by business ID.
     *
     * @param businessId the business ID
     * @return List of active payment plans for the specified business
     */
    List<PaymentPlan> findByBusinessIdAndIsActiveTrue(Long businessId);

    /**
     * Find payment plans by period type.
     *
     * @param periodType the period type
     * @return List of payment plans with the specified period type
     */
    List<PaymentPlan> findByPeriodType(PeriodType periodType);

    /**
     * Find active payment plans by period type.
     *
     * @param periodType the period type
     * @return List of active payment plans with the specified period type
     */
    List<PaymentPlan> findByPeriodTypeAndIsActiveTrue(PeriodType periodType);

    /**
     * Find payment plans by business ID and period type.
     *
     * @param businessId the business ID
     * @param periodType the period type
     * @return List of payment plans matching the criteria
     */
    List<PaymentPlan> findByBusinessIdAndPeriodType(Long businessId, PeriodType periodType);

    /**
     * Find active payment plans by business ID and period type.
     *
     * @param businessId the business ID
     * @param periodType the period type
     * @return List of active payment plans matching the criteria
     */
    @Query("SELECT p FROM PaymentPlan p WHERE p.business.id = :businessId AND " +
           "p.periodType = :periodType AND p.isActive = true")
    List<PaymentPlan> findActiveByBusinessAndPeriodType(@Param("businessId") Long businessId, 
                                                         @Param("periodType") PeriodType periodType);

    /**
     * Find all active payment plans.
     *
     * @return List of all active payment plans
     */
    List<PaymentPlan> findByIsActiveTrue();

    /**
     * Find payment plans by plan name containing search term (case-insensitive).
     *
     * @param searchTerm the search term
     * @return List of payment plans matching the search criteria
     */
    List<PaymentPlan> findByPlanNameContainingIgnoreCase(String searchTerm);

    /**
     * Count payment plans by business ID.
     *
     * @param businessId the business ID
     * @return Count of payment plans for the specified business
     */
    long countByBusinessId(Long businessId);

    /**
     * Count active payment plans by business ID.
     *
     * @param businessId the business ID
     * @return Count of active payment plans for the specified business
     */
    long countByBusinessIdAndIsActiveTrue(Long businessId);

    /**
     * Find payment plans with discounts by business ID.
     *
     * @param businessId the business ID
     * @return List of payment plans with discounts
     */
    @Query("SELECT p FROM PaymentPlan p WHERE p.business.id = :businessId AND " +
           "p.discountPercentage > 0 AND p.isActive = true")
    List<PaymentPlan> findPlansWithDiscountsByBusiness(@Param("businessId") Long businessId);
    
    /**
     * Find payment plans by business ID and active status.
     *
     * @param businessId the business ID
     * @param isActive the active status
     * @return List of payment plans matching the criteria
     */
    List<PaymentPlan> findByBusinessIdAndIsActive(Long businessId, Boolean isActive);
}
