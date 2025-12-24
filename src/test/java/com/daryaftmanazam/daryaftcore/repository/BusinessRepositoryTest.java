package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Business;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BusinessRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class BusinessRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BusinessRepository businessRepository;

    private Business activeBusiness;
    private Business inactiveBusiness;

    @BeforeEach
    void setUp() {
        // Create and persist test data
        activeBusiness = Business.builder()
                .businessName("Active Gym")
                .ownerName("John Doe")
                .contactEmail("john@activegym.com")
                .contactPhone("09123456789")
                .description("A fitness center")
                .isActive(true)
                .build();

        inactiveBusiness = Business.builder()
                .businessName("Inactive Gym")
                .ownerName("Jane Smith")
                .contactEmail("jane@inactivegym.com")
                .contactPhone("09987654321")
                .description("A closed fitness center")
                .isActive(false)
                .build();

        entityManager.persist(activeBusiness);
        entityManager.persist(inactiveBusiness);
        entityManager.flush();
    }

    @Test
    void testFindByBusinessName_ShouldReturnBusiness() {
        // When
        Optional<Business> found = businessRepository.findByBusinessName("Active Gym");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getOwnerName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByBusinessName_ShouldReturnEmpty_WhenNotFound() {
        // When
        Optional<Business> found = businessRepository.findByBusinessName("Non-existent Gym");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByIsActiveTrue_ShouldReturnOnlyActiveBusinesses() {
        // When
        List<Business> activeBusinesses = businessRepository.findByIsActiveTrue();

        // Then
        assertThat(activeBusinesses).hasSize(1);
        assertThat(activeBusinesses.get(0).getBusinessName()).isEqualTo("Active Gym");
    }

    @Test
    void testFindByIsActiveFalse_ShouldReturnOnlyInactiveBusinesses() {
        // When
        List<Business> inactiveBusinesses = businessRepository.findByIsActiveFalse();

        // Then
        assertThat(inactiveBusinesses).hasSize(1);
        assertThat(inactiveBusinesses.get(0).getBusinessName()).isEqualTo("Inactive Gym");
    }

    @Test
    void testCountTotalBusinesses_ShouldReturnCorrectCount() {
        // When
        long count = businessRepository.countTotalBusinesses();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByIsActiveTrue_ShouldReturnCorrectCount() {
        // When
        long count = businessRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testFindByBusinessNameContainingIgnoreCase_ShouldReturnMatchingBusinesses() {
        // When
        List<Business> found = businessRepository.findByBusinessNameContainingIgnoreCase("gym");

        // Then
        assertThat(found).hasSize(2);
    }

    @Test
    void testFindByOwnerName_ShouldReturnBusinesses() {
        // When
        List<Business> found = businessRepository.findByOwnerName("John Doe");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getBusinessName()).isEqualTo("Active Gym");
    }

    @Test
    void testFindByContactEmail_ShouldReturnBusiness() {
        // When
        Optional<Business> found = businessRepository.findByContactEmail("john@activegym.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBusinessName()).isEqualTo("Active Gym");
    }

    @Test
    void testFindByContactPhone_ShouldReturnBusiness() {
        // When
        Optional<Business> found = businessRepository.findByContactPhone("09123456789");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBusinessName()).isEqualTo("Active Gym");
    }

    @Test
    void testSaveAndFlush_ShouldPersistBusiness() {
        // Given
        Business newBusiness = Business.builder()
                .businessName("New Gym")
                .ownerName("Mike Johnson")
                .contactEmail("mike@newgym.com")
                .contactPhone("09111111111")
                .isActive(true)
                .build();

        // When
        Business saved = businessRepository.saveAndFlush(newBusiness);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(businessRepository.findById(saved.getId())).isPresent();
    }
}
