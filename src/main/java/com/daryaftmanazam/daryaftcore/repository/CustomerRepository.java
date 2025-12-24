package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entity operations.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customers by business ID.
     *
     * @param businessId the business ID
     * @return List of customers belonging to the business
     */
    List<Customer> findByBusinessId(Long businessId);

    /**
     * Find a customer by phone number.
     *
     * @param phoneNumber the phone number
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);

    /**
     * Find active customers by business ID.
     *
     * @param businessId the business ID
     * @return List of active customers for the specified business
     */
    List<Customer> findByBusinessIdAndIsActiveTrue(Long businessId);

    /**
     * Find customers by customer type and business ID.
     *
     * @param customerType the customer type
     * @param businessId   the business ID
     * @return List of customers matching the criteria
     */
    List<Customer> findByCustomerTypeAndBusinessId(CustomerType customerType, Long businessId);

    /**
     * Search customers by name (first name or last name contains search term).
     *
     * @param searchTerm the search term
     * @return List of customers matching the search criteria
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Search customers by name within a specific business.
     *
     * @param searchTerm the search term
     * @param businessId the business ID
     * @return List of customers matching the search criteria
     */
    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> searchByNameAndBusiness(@Param("searchTerm") String searchTerm, 
                                           @Param("businessId") Long businessId);

    /**
     * Find all active customers.
     *
     * @return List of all active customers
     */
    List<Customer> findByIsActiveTrue();

    /**
     * Find customers by email.
     *
     * @param email the email address
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Count customers by business ID.
     *
     * @param businessId the business ID
     * @return Count of customers for the specified business
     */
    long countByBusinessId(Long businessId);

    /**
     * Count active customers by business ID.
     *
     * @param businessId the business ID
     * @return Count of active customers for the specified business
     */
    long countByBusinessIdAndIsActiveTrue(Long businessId);
    
    /**
     * Count customers by business ID and isActive status.
     *
     * @param businessId the business ID
     * @param isActive the active status
     * @return Count of customers matching the criteria
     */
    long countByBusinessIdAndIsActive(Long businessId, Boolean isActive);
    
    /**
     * Find customers by business ID with pagination.
     *
     * @param businessId the business ID
     * @param pageable pagination information
     * @return Page of customers
     */
    Page<Customer> findByBusinessId(Long businessId, Pageable pageable);
    
    /**
     * Search customers by keyword (name or phone) within a business.
     *
     * @param businessId the business ID
     * @param keyword the search keyword
     * @return List of customers matching the search criteria
     */
    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.phoneNumber LIKE CONCAT('%', :keyword, '%'))")
    List<Customer> findByBusinessIdAndKeyword(@Param("businessId") Long businessId, 
                                              @Param("keyword") String keyword);

    /**
     * Find customers by business ID and customer type.
     *
     * @param businessId   the business ID
     * @param customerType the customer type
     * @return List of customers matching the criteria
     */
    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId AND c.customerType = :customerType AND c.isActive = true")
    List<Customer> findActiveCustomersByBusinessAndType(@Param("businessId") Long businessId, 
                                                         @Param("customerType") CustomerType customerType);
}
