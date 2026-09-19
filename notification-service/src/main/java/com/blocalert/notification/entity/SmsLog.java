package com.blocalert.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sms_logs")
public class SmsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "message_UUID", length = 100)
    private String messageUUId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createdAt;
}
