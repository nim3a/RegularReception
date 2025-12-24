package com.daryaftmanazam.daryaftcore.model;

import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a payment plan entity.
 */
@Entity
@Table(name = "payment_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name")
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type")
    private PeriodType periodType;

    @Column(name = "period_count")
    private Integer periodCount;

    @Column(name = "base_amount", precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "late_fee_per_day", precision = 19, scale = 2)
    private BigDecimal lateFeePerDay;

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}
