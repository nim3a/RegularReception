package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.dto.request.BusinessRequest;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessDashboardResponse;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessResponse;
import com.daryaftmanazam.daryaftcore.exception.BusinessNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.BusinessRepository;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Service for managing business operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BusinessService {
    
    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    
    /**
     * Create a new business.
     */
    @Transactional
    public BusinessResponse createBusiness(BusinessRequest request) {
        log.info("Creating new business: {}", request.getBusinessName());
        
        Business business = Business.builder()
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .description(request.getDescription())
                .isActive(true)
                .build();
        
        Business savedBusiness = businessRepository.save(business);
        log.info("Business created successfully with id: {}", savedBusiness.getId());
        
        return mapToResponse(savedBusiness);
    }
    
    /**
     * Get business details by ID.
     */
    public BusinessResponse getBusinessById(Long id) {
        log.debug("Fetching business with id: {}", id);
        
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException(id));
        
        return mapToResponse(business);
    }
    
    /**
     * Update business information.
     */
    @Transactional
    public BusinessResponse updateBusiness(Long id, BusinessRequest request) {
        log.info("Updating business with id: {}", id);
        
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException(id));
        
        business.setBusinessName(request.getBusinessName());
        business.setOwnerName(request.getOwnerName());
        business.setContactEmail(request.getContactEmail());
        business.setContactPhone(request.getContactPhone());
        business.setDescription(request.getDescription());
        
        Business updatedBusiness = businessRepository.save(business);
        log.info("Business updated successfully: {}", updatedBusiness.getId());
        
        return mapToResponse(updatedBusiness);
    }
    
    /**
     * Soft delete a business (set isActive to false).
     */
    @Transactional
    public void deleteBusiness(Long id) {
        log.info("Soft deleting business with id: {}", id);
        
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException(id));
        
        business.setIsActive(false);
        businessRepository.save(business);
        
        log.info("Business soft deleted successfully: {}", id);
    }
    
    /**
     * Get all businesses with pagination.
     */
    public Page<BusinessResponse> getAllBusinesses(Pageable pageable) {
        log.debug("Fetching all businesses with pagination");
        
        Page<Business> businesses = businessRepository.findAll(pageable);
        return businesses.map(this::mapToResponse);
    }
    
    /**
     * Get business dashboard statistics.
     */
    public BusinessDashboardResponse getBusinessDashboard(Long businessId) {
        log.debug("Fetching dashboard for business: {}", businessId);
        
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
        
        // Get statistics
        int totalCustomers = (int) customerRepository.countByBusinessId(businessId);
        int activeCustomers = (int) customerRepository.countByBusinessIdAndIsActive(businessId, true);
        int activeSubscriptions = subscriptionRepository.countByCustomerBusinessIdAndStatus(
                businessId, SubscriptionStatus.ACTIVE);
        int overdueSubscriptions = subscriptionRepository.countByCustomerBusinessIdAndStatus(
                businessId, SubscriptionStatus.OVERDUE);
        
        // Calculate financial metrics
        BigDecimal totalRevenue = paymentRepository.sumAmountByBusinessId(businessId);
        BigDecimal pendingPayments = subscriptionRepository.sumPendingPaymentsByBusinessId(businessId);
        BigDecimal overdueAmount = subscriptionRepository.sumOverdueAmountByBusinessId(businessId);
        
        return BusinessDashboardResponse.builder()
                .businessId(business.getId())
                .businessName(business.getBusinessName())
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .activeSubscriptions(activeSubscriptions)
                .overdueSubscriptions(overdueSubscriptions)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .pendingPayments(pendingPayments != null ? pendingPayments : BigDecimal.ZERO)
                .overdueAmount(overdueAmount != null ? overdueAmount : BigDecimal.ZERO)
                .build();
    }
    
    /**
     * Map Business entity to BusinessResponse DTO.
     */
    private BusinessResponse mapToResponse(Business business) {
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
                .customerCount(business.getCustomers() != null ? (long) business.getCustomers().size() : 0L)
                .build();
    }
}
