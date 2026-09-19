package com.blocalert.notification.entity;

import com.blocalert.enums.AlertChannel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "message_templates",
       indexes = {
        @Index(name = "idx_templates_channel_code", columnList = "channel,code", unique = true)
})
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel",nullable = false)
    private AlertChannel channel;

    @Column(name = "subject")
    private String subject;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime createdAt;
}
