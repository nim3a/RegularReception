package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CustomerRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Business business1;
    private Business business2;
    private Customer activeCustomer1;
    private Customer activeCustomer2;
    private Customer inactiveCustomer;

    @BeforeEach
    void setUp() {
        // Create businesses
        business1 = Business.builder()
                .businessName("Gym 1")
                .ownerName("Owner 1")
                .contactEmail("owner1@gym.com")
                .contactPhone("09111111111")
                .isActive(true)
                .build();

        business2 = Business.builder()
                .businessName("Gym 2")
                .ownerName("Owner 2")
                .contactEmail("owner2@gym.com")
                .contactPhone("09222222222")
                .isActive(true)
                .build();

        entityManager.persist(business1);
        entityManager.persist(business2);

        // Create customers
        activeCustomer1 = Customer.builder()
                .firstName("Ali")
                .lastName("Ahmadi")
                .phoneNumber("09123456789")
                .email("ali@example.com")
                .customerType(CustomerType.REGULAR)
                .isActive(true)
                .joinDate(LocalDate.now())
                .business(business1)
                .build();

        activeCustomer2 = Customer.builder()
                .firstName("Sara")
                .lastName("Karimi")
                .phoneNumber("09987654321")
                .email("sara@example.com")
                .customerType(CustomerType.VIP)
                .isActive(true)
                .joinDate(LocalDate.now())
                .business(business1)
                .build();

        inactiveCustomer = Customer.builder()
                .firstName("Mohammad")
                .lastName("Rezaei")
                .phoneNumber("09555555555")
                .email("mohammad@example.com")
                .customerType(CustomerType.NEW)
                .isActive(false)
                .joinDate(LocalDate.now().minusMonths(6))
                .business(business2)
                .build();

        entityManager.persist(activeCustomer1);
        entityManager.persist(activeCustomer2);
        entityManager.persist(inactiveCustomer);
        entityManager.flush();
    }

    @Test
    void testFindByBusinessId_ShouldReturnCustomersForBusiness() {
        // When
        List<Customer> customers = customerRepository.findByBusinessId(business1.getId());

        // Then
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(Customer::getFirstName)
                .containsExactlyInAnyOrder("Ali", "Sara");
    }

    @Test
    void testFindByPhoneNumber_ShouldReturnCustomer() {
        // When
        Optional<Customer> found = customerRepository.findByPhoneNumber("09123456789");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Ali");
    }

    @Test
    void testFindByBusinessIdAndIsActiveTrue_ShouldReturnOnlyActiveCustomers() {
        // When
        List<Customer> activeCustomers = customerRepository.findByBusinessIdAndIsActiveTrue(business1.getId());

        // Then
        assertThat(activeCustomers).hasSize(2);
        assertThat(activeCustomers).allMatch(Customer::getIsActive);
    }

    @Test
    void testFindByCustomerTypeAndBusinessId_ShouldReturnMatchingCustomers() {
        // When
        List<Customer> regularCustomers = customerRepository.findByCustomerTypeAndBusinessId(
                CustomerType.REGULAR, business1.getId());

        // Then
        assertThat(regularCustomers).hasSize(1);
        assertThat(regularCustomers.get(0).getFirstName()).isEqualTo("Ali");
    }

    @Test
    void testSearchByName_ShouldReturnMatchingCustomers() {
        // When
        List<Customer> found = customerRepository.searchByName("ali");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFirstName()).isEqualTo("Ali");
    }

    @Test
    void testSearchByName_ShouldSearchLastName() {
        // When
        List<Customer> found = customerRepository.searchByName("karimi");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getLastName()).isEqualTo("Karimi");
    }

    @Test
    void testSearchByNameAndBusiness_ShouldReturnMatchingCustomers() {
        // When
        List<Customer> found = customerRepository.searchByNameAndBusiness("ali", business1.getId());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFirstName()).isEqualTo("Ali");
    }

    @Test
    void testFindByIsActiveTrue_ShouldReturnAllActiveCustomers() {
        // When
        List<Customer> activeCustomers = customerRepository.findByIsActiveTrue();

        // Then
        assertThat(activeCustomers).hasSize(2);
        assertThat(activeCustomers).allMatch(Customer::getIsActive);
    }

    @Test
    void testFindByEmail_ShouldReturnCustomer() {
        // When
        Optional<Customer> found = customerRepository.findByEmail("ali@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Ali");
    }

    @Test
    void testCountByBusinessId_ShouldReturnCorrectCount() {
        // When
        long count = customerRepository.countByBusinessId(business1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByBusinessIdAndIsActiveTrue_ShouldReturnCorrectCount() {
        // When
        long count = customerRepository.countByBusinessIdAndIsActiveTrue(business1.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testFindActiveCustomersByBusinessAndType_ShouldReturnMatchingCustomers() {
        // When
        List<Customer> vipCustomers = customerRepository.findActiveCustomersByBusinessAndType(
                business1.getId(), CustomerType.VIP);

        // Then
        assertThat(vipCustomers).hasSize(1);
        assertThat(vipCustomers.get(0).getFirstName()).isEqualTo("Sara");
    }

    @Test
    void testSaveCustomer_ShouldPersistCustomer() {
        // Given
        Customer newCustomer = Customer.builder()
                .firstName("Reza")
                .lastName("Mohammadi")
                .phoneNumber("09333333333")
                .email("reza@example.com")
                .customerType(CustomerType.NEW)
                .isActive(true)
                .joinDate(LocalDate.now())
                .business(business1)
                .build();

        // When
        Customer saved = customerRepository.saveAndFlush(newCustomer);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(customerRepository.findById(saved.getId())).isPresent();
    }
}
