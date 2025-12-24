package com.daryaftmanazam.daryaftcore.util.mapper;

import com.daryaftmanazam.daryaftcore.dto.request.CustomerRequest;
import com.daryaftmanazam.daryaftcore.dto.response.CustomerResponse;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Mapper utility for converting between Customer entity and DTOs.
 */
@UtilityClass
public class CustomerMapper {

    /**
     * Convert CustomerRequest to Customer entity.
     */
    public Customer toEntity(CustomerRequest request) {
        if (request == null) {
            return null;
        }

        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .customerType(request.getCustomerType())
                .isActive(true)
                .joinDate(LocalDate.now())
                .build();
    }

    /**
     * Update Customer entity from CustomerRequest.
     */
    public void updateEntity(CustomerRequest request, Customer customer) {
        if (request == null || customer == null) {
            return;
        }

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getCustomerType() != null) {
            customer.setCustomerType(request.getCustomerType());
        }
    }

    /**
     * Convert Customer entity to CustomerResponse.
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        String fullName = customer.getFirstName() + " " + customer.getLastName();
        
        Subscription currentSubscription = customer.getSubscriptions() != null && !customer.getSubscriptions().isEmpty() ?
            customer.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.OVERDUE)
                .max(Comparator.comparing(Subscription::getCreatedAt))
                .orElse(null) : null;

        SubscriptionStatus currentStatus = currentSubscription != null ? currentSubscription.getStatus() : null;
        boolean hasActiveSubscription = currentStatus == SubscriptionStatus.ACTIVE;
        int totalSubscriptions = customer.getSubscriptions() != null ? customer.getSubscriptions().size() : 0;

        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(fullName)
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .customerType(customer.getCustomerType())
                .isActive(customer.getIsActive())
                .joinDate(customer.getJoinDate())
                .createdAt(customer.getCreatedAt())
                .businessId(customer.getBusiness() != null ? customer.getBusiness().getId() : null)
                .businessName(customer.getBusiness() != null ? customer.getBusiness().getBusinessName() : null)
                .currentSubscriptionStatus(currentStatus)
                .hasActiveSubscription(hasActiveSubscription)
                .totalSubscriptions(totalSubscriptions)
                .build();
    }
}
