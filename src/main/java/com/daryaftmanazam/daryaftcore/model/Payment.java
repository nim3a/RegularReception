package com.daryaftmanazam.daryaftcore.model;

import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a payment entity.
 */
@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_transaction_id", columnNames = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "late_fee", precision = 19, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
