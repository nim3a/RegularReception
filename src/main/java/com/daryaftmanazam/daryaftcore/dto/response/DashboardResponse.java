package com.daryaftmanazam.daryaftcore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for dashboard statistics and metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Financial metrics
    @JsonProperty("total_revenue")
    private BigDecimal totalRevenue;

    @JsonProperty("monthly_revenue")
    private BigDecimal monthlyRevenue;

    @JsonProperty("pending_payments")
    private BigDecimal pendingPayments;

    @JsonProperty("overdue_amount")
    private BigDecimal overdueAmount;

    // Subscription metrics
    @JsonProperty("active_subscriptions")
    private Long activeSubscriptions;

    @JsonProperty("pending_subscriptions")
    private Long pendingSubscriptions;

    @JsonProperty("overdue_subscriptions")
    private Long overdueSubscriptions;

    @JsonProperty("cancelled_subscriptions")
    private Long cancelledSubscriptions;

    @JsonProperty("total_subscriptions")
    private Long totalSubscriptions;

    // Customer metrics
    @JsonProperty("total_customers")
    private Long totalCustomers;

    @JsonProperty("active_customers")
    private Long activeCustomers;

    @JsonProperty("new_customers_this_month")
    private Long newCustomersThisMonth;

    @JsonProperty("vip_customers")
    private Long vipCustomers;

    // Payment metrics
    @JsonProperty("total_payments")
    private Long totalPayments;

    @JsonProperty("completed_payments")
    private Long completedPayments;

    @JsonProperty("failed_payments")
    private Long failedPayments;

    @JsonProperty("payments_this_month")
    private Long paymentsThisMonth;

    // Overdue metrics
    @JsonProperty("overdue_count")
    private Long overdueCount;

    @JsonProperty("overdue_within_grace_period")
    private Long overdueWithinGracePeriod;

    @JsonProperty("overdue_past_grace_period")
    private Long overduePastGracePeriod;

    // Growth metrics
    @JsonProperty("revenue_growth_percentage")
    private BigDecimal revenueGrowthPercentage;

    @JsonProperty("customer_growth_percentage")
    private BigDecimal customerGrowthPercentage;

    @JsonProperty("subscription_growth_percentage")
    private BigDecimal subscriptionGrowthPercentage;
}
