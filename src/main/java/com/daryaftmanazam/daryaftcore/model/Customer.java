package com.daryaftmanazam.daryaftcore.model;

import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer entity.
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
    @UniqueConstraint(name = "uk_customer_phone", columnNames = "phone_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotNull(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotNull(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Builder.Default
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    /**
     * Helper method to add a subscription to the customer.
     */
    public void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
        subscription.setCustomer(this);
    }

    /**
     * Helper method to remove a subscription from the customer.
     */
    public void removeSubscription(Subscription subscription) {
        subscriptions.remove(subscription);
        subscription.setCustomer(null);
    }

    /**
     * Helper method to get full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Helper method for backward compatibility.
     */
    public String getPhone() {
        return phoneNumber;
    }
}
