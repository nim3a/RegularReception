package com.daryaftmanazam.daryaftcore.util.mapper;

import com.daryaftmanazam.daryaftcore.dto.request.BusinessRequest;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessResponse;
import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import lombok.experimental.UtilityClass;

/**
 * Mapper utility for converting between Business entity and DTOs.
 */
@UtilityClass
public class BusinessMapper {

    /**
     * Convert BusinessRequest to Business entity.
     */
    public Business toEntity(BusinessRequest request) {
        if (request == null) {
            return null;
        }

        return Business.builder()
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .description(request.getDescription())
                .isActive(true)
                .build();
    }

    /**
     * Update Business entity from BusinessRequest.
     */
    public void updateEntity(BusinessRequest request, Business business) {
        if (request == null || business == null) {
            return;
        }

        if (request.getBusinessName() != null) {
            business.setBusinessName(request.getBusinessName());
        }
        if (request.getOwnerName() != null) {
            business.setOwnerName(request.getOwnerName());
        }
        if (request.getContactEmail() != null) {
            business.setContactEmail(request.getContactEmail());
        }
        if (request.getContactPhone() != null) {
            business.setContactPhone(request.getContactPhone());
        }
        if (request.getDescription() != null) {
            business.setDescription(request.getDescription());
        }
    }

    /**
     * Convert Business entity to BusinessResponse.
     */
    public BusinessResponse toResponse(Business business) {
        if (business == null) {
            return null;
        }

        long customerCount = business.getCustomers() != null ? business.getCustomers().size() : 0;
        
        long activeSubscriptionsCount = business.getCustomers() != null ? 
            business.getCustomers().stream()
                .flatMap(customer -> customer.getSubscriptions().stream())
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE)
                .count() : 0;

        return BusinessResponse.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .ownerName(business.getOwnerName())
                .contactEmail(business.getContactEmail())
                .contactPhone(business.getContactPhone())
                .description(business.getDescription())
                .isActive(business.getIsActive())
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .customerCount(customerCount)
                .activeSubscriptionsCount(activeSubscriptionsCount)
                .build();
    }
}
