package com.daryaftmanazam.daryaftcore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a business entity in the system.
 */
@Entity
@Table(name = "businesses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Business name is required")
    @Size(max = 100, message = "Business name must not exceed 100 characters")
    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @NotNull(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Email(message = "Invalid email format")
    @Column(name = "contact_email")
    private String contactEmail;

    @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be in Iranian format: 09xxxxxxxxx")
    @Column(name = "contact_phone")
    private String contactPhone;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Customer> customers = new ArrayList<>();

    /**
     * SMS configuration for this business.
     * Contains provider-specific settings for sending SMS notifications.
     */
    @Embedded
    private SmsConfig smsConfig;

    /**
     * Helper method to add a customer to the business.
     */
    public void addCustomer(Customer customer) {
        customers.add(customer);
        customer.setBusiness(this);
    }

    /**
     * Helper method to remove a customer from the business.
     */
    public void removeCustomer(Customer customer) {
        customers.remove(customer);
        customer.setBusiness(null);
    }
}
