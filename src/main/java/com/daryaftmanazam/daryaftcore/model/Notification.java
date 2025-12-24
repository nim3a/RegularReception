package com.daryaftmanazam.daryaftcore.model;

import com.daryaftmanazam.daryaftcore.model.enums.Channel;
import com.daryaftmanazam.daryaftcore.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a notification entity.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private Channel channel;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    /**
     * Notification status enum.
     */
    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }
}
