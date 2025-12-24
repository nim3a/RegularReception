package com.daryaftmanazam.daryaftcore.model;

import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a subscription entity.
 */
@Entity
@Table(name = "subscriptions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_plan_id", nullable = false)
    private PaymentPlan paymentPlan;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubscriptionStatus status;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_applied", precision = 19, scale = 2)
    private BigDecimal discountApplied;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_reminder_sent")
    private LocalDateTime lastReminderSent;

    @Builder.Default
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    /**
     * Helper method to add a payment to the subscription.
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setSubscription(this);
    }

    /**
     * Helper method to remove a payment from the subscription.
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setSubscription(null);
    }
}
