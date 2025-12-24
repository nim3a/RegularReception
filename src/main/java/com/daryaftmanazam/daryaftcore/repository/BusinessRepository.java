package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Business entity operations.
 */
@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    /**
     * Find a business by its business name.
     *
     * @param businessName the name of the business
     * @return Optional containing the business if found
     */
    Optional<Business> findByBusinessName(String businessName);

    /**
     * Find all active businesses.
     *
     * @return List of active businesses
     */
    List<Business> findByIsActiveTrue();

    /**
     * Find all inactive businesses.
     *
     * @return List of inactive businesses
     */
    List<Business> findByIsActiveFalse();

    /**
     * Count total number of businesses.
     *
     * @return Total count of businesses
     */
    @Query("SELECT COUNT(b) FROM Business b")
    long countTotalBusinesses();

    /**
     * Count active businesses.
     *
     * @return Count of active businesses
     */
    long countByIsActiveTrue();

    /**
     * Search businesses by name containing the search term (case-insensitive).
     *
     * @param searchTerm the search term
     * @return List of matching businesses
     */
    List<Business> findByBusinessNameContainingIgnoreCase(String searchTerm);

    /**
     * Find businesses by owner name.
     *
     * @param ownerName the owner name
     * @return List of businesses owned by the specified owner
     */
    List<Business> findByOwnerName(String ownerName);

    /**
     * Find a business by contact email.
     *
     * @param contactEmail the contact email
     * @return Optional containing the business if found
     */
    Optional<Business> findByContactEmail(String contactEmail);

    /**
     * Find a business by contact phone.
     *
     * @param contactPhone the contact phone number
     * @return Optional containing the business if found
     */
    Optional<Business> findByContactPhone(String contactPhone);
}
