package com.blocalert.alert.entity;

import com.blocalert.enums.AlertCondition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_user_crypto_id", columnList = "user_id,crypto_id")
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "crypto_id", nullable = false, length = 20)
    private String cryptoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_condition", nullable = false)
    private AlertCondition condition;

    @Column(name = "threshold_value", nullable = false, precision = 20, scale = 8)
    private BigDecimal thresholdValue;

    @Column(name = "notification_websocket")
    private Boolean notificationWebsocket = true;

    @Column(name = "notification_email")
    private Boolean notificationEmail = true;

    @Column(name = "notification_sms")
    private Boolean notificationSms = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_triggered")
    private Boolean isTriggered = false;

    @Column(name = "triggered_at", columnDefinition = "TIMESTAMP(0)")   // TODO : DATETIME
    private LocalDateTime triggeredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(0)")   // TODO : DATETIME
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP(0)")   // TODO : DATETIME
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "alert", fetch = FetchType.LAZY)
    private List<AlertDelivery> alertDeliveries = new ArrayList<>();

}
