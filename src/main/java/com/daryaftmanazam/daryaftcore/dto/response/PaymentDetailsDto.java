package com.daryaftmanazam.daryaftcore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment gateway details page.
 * Contains all information needed to display the payment confirmation page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsDto {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("customer")
    private CustomerInfo customer;

    @JsonProperty("plan")
    private PlanInfo plan;

    /**
     * Customer information for payment gateway.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("full_name")
        private String fullName;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("phone")
        private String phone;
    }

    /**
     * Payment plan information for payment gateway.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanInfo {
        
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("duration")
        private Integer duration;
    }
}
