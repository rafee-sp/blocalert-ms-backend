package com.blocalert.user.entity;

import com.blocalert.user.entity.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "subscription")
@ToString(exclude = "user")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "subscription_id", length = 100)
    private String subscriptionId;

    @Column(name = "correlation_id", length = 40, unique = true, nullable = false)
    private String correlationId;

    @Column(name = "invoice_id", length = 100)
    private String invoiceId;

    @Column(name = "invoice_url", length = 512)
    private String invoiceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.PENDING;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "current_subscription_start", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime currentSubscriptionStart;

    @Column(name = "current_subscription_end", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime currentSubscriptionEnd;

    @Column(name = "is_cancelled")
    private Boolean isCancelled = false;

    @Column(name = "activated_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime activatedAt;

    @Column(name = "cancelled_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime canceledAt;

    @Column(name = "expired_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime expiredAt;

    @Column(name = "last_renewed_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime lastRenewedAt;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime updatedAt;
}
