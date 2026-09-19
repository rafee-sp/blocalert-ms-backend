package com.blocalert.alert.entity;

import com.blocalert.enums.AlertChannel;
import com.blocalert.enums.AlertChannelStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "alert_deliveries")
@ToString(exclude = "alert")
public class AlertDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Alert alert;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_channel", nullable = false)
    private AlertChannel alertChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", nullable = false)
    private AlertChannelStatus alertStatus;

    @CreationTimestamp
    @Column(name = "triggered_at", nullable = false, columnDefinition = "TIMESTAMP(0)")   // TODO : DATETIME
    private LocalDateTime triggeredAt;

    @Column(name = "delivered_at", columnDefinition = "TIMESTAMP(0)")  // TODO : DATETIME
    private LocalDateTime deliveredAt;

}
