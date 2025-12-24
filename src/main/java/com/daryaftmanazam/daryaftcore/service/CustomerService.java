package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.dto.request.CustomerRequest;
import com.daryaftmanazam.daryaftcore.dto.response.CustomerResponse;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.exception.BusinessNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.CustomerNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.BusinessRepository;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing customer operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionRepository subscriptionRepository;
    
    /**
     * Add a new customer to a business.
     */
    @Transactional
    public CustomerResponse addCustomer(Long businessId, CustomerRequest request) {
        log.info("Adding new customer to business: {}", businessId);
        
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
        
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .customerType(request.getCustomerType())
                .joinDate(request.getJoinDate() != null ? request.getJoinDate() : LocalDate.now())
                .isActive(true)
                .business(business)
                .build();
        
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with id: {}", savedCustomer.getId());
        
        return mapToResponse(savedCustomer);
    }
    
    /**
     * Get customer details by ID.
     */
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer with id: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        
        return mapToResponse(customer);
    }
    
    /**
     * Update customer information.
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Updating customer with id: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        customer.setCustomerType(request.getCustomerType());
        if (request.getJoinDate() != null) {
            customer.setJoinDate(request.getJoinDate());
        }
        
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully: {}", updatedCustomer.getId());
        
        return mapToResponse(updatedCustomer);
    }
    
    /**
     * Soft delete a customer (set isActive to false).
     */
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Soft deleting customer with id: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        
        customer.setIsActive(false);
        customerRepository.save(customer);
        
        log.info("Customer soft deleted successfully: {}", id);
    }
    
    /**
     * Get customers by business with pagination.
     */
    public Page<CustomerResponse> getCustomersByBusiness(Long businessId, Pageable pageable) {
        log.debug("Fetching customers for business: {}", businessId);
        
        // Verify business exists
        if (!businessRepository.existsById(businessId)) {
            throw new BusinessNotFoundException(businessId);
        }
        
        Page<Customer> customers = customerRepository.findByBusinessId(businessId, pageable);
        return customers.map(this::mapToResponse);
    }
    
    /**
     * Search customers by name or phone number.
     */
    public List<CustomerResponse> searchCustomers(Long businessId, String keyword) {
        log.debug("Searching customers in business {} with keyword: {}", businessId, keyword);
        
        // Verify business exists
        if (!businessRepository.existsById(businessId)) {
            throw new BusinessNotFoundException(businessId);
        }
        
        List<Customer> customers = customerRepository.findByBusinessIdAndKeyword(businessId, keyword);
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get customer subscription history.
     */
    public List<SubscriptionResponse> getCustomerSubscriptionHistory(Long customerId) {
        log.debug("Fetching subscription history for customer: {}", customerId);
        
        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        
        List<Subscription> subscriptions = subscriptionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return subscriptions.stream()
                .map(this::mapSubscriptionToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map Customer entity to CustomerResponse DTO.
     */
    private CustomerResponse mapToResponse(Customer customer) {
        int activeSubscriptions = (int) customer.getSubscriptions().stream()
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .count();
        
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .customerType(customer.getCustomerType())
                .isActive(customer.getIsActive())
                .joinDate(customer.getJoinDate())
                .createdAt(customer.getCreatedAt())
                .businessId(customer.getBusiness().getId())
                .businessName(customer.getBusiness().getBusinessName())
                .activeSubscriptionsCount(activeSubscriptions)
                .build();
    }
    
    /**
     * Map Subscription entity to SubscriptionResponse DTO.
     */
    private SubscriptionResponse mapSubscriptionToResponse(Subscription subscription) {
        Integer daysOverdue = null;
        if (subscription.getStatus() == SubscriptionStatus.OVERDUE && 
            subscription.getNextPaymentDate() != null) {
            daysOverdue = (int) ChronoUnit.DAYS.between(subscription.getNextPaymentDate(), LocalDate.now());
        }
        
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .customerId(subscription.getCustomer().getId())
                .customerName(subscription.getCustomer().getFirstName() + " " + 
                            subscription.getCustomer().getLastName())
                .paymentPlanId(subscription.getPaymentPlan().getId())
                .paymentPlanName(subscription.getPaymentPlan().getPlanName())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .totalAmount(subscription.getTotalAmount())
                .discountApplied(subscription.getDiscountApplied())
                .nextPaymentDate(subscription.getNextPaymentDate())
                .lastPaymentDate(subscription.getLastPaymentDate())
                .createdAt(subscription.getCreatedAt())
                .daysOverdue(daysOverdue)
                .build();
    }
}
