package com.daryaftmanazam.daryaftcore.dto.response;

import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for customer entity with subscription status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("customer_type")
    private CustomerType customerType;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("join_date")
    private LocalDate joinDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("business_id")
    private Long businessId;

    @JsonProperty("business_name")
    private String businessName;

    // Current subscription status
    @JsonProperty("current_subscription_status")
    private SubscriptionStatus currentSubscriptionStatus;

    @JsonProperty("has_active_subscription")
    private Boolean hasActiveSubscription;

    @JsonProperty("total_subscriptions")
    private Integer totalSubscriptions;
    
    @JsonProperty("active_subscriptions_count")
    private Integer activeSubscriptionsCount;
}
