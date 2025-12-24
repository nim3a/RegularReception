package com.daryaftmanazam.daryaftcore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for business dashboard statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDashboardResponse {
    
    private Long businessId;
    private String businessName;
    private Integer totalCustomers;
    private Integer activeCustomers;
    private Integer activeSubscriptions;
    private Integer overdueSubscriptions;
    private BigDecimal totalRevenue;
    private BigDecimal pendingPayments;
    private BigDecimal overdueAmount;
}
