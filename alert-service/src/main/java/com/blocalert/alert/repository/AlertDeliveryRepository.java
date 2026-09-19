package com.blocalert.alert.repository;

import com.blocalert.alert.entity.AlertDelivery;
import com.blocalert.enums.AlertChannel;
import com.blocalert.enums.AlertChannelStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

public interface AlertDeliveryRepository extends JpaRepository<AlertDelivery, Long> {

    @Modifying
    @Query(value = """
    INSERT INTO alert_delivery
        (alert_id, alert_channel, send_at, alert_status, delivered_at)
    VALUES
        (:alertId, :channel, :triggeredAt, :status, :deliveredAt)
    ON DUPLICATE KEY UPDATE
        alert_status = CASE
            WHEN alert_status = 'PENDING'
                 AND VALUES(alert_status) IN ('DELIVERED', 'FAILED')
            THEN VALUES(alert_status)
            ELSE alert_status
        END,
        delivered_at = CASE
            WHEN VALUES(alert_status) = 'DELIVERED'
            THEN COALESCE(delivered_at, VALUES(delivered_at))
            ELSE delivered_at
        END
    """, nativeQuery = true)
    void upsertDelivery(Long alertId, AlertChannel channel, AlertChannelStatus status, LocalDateTime triggeredAt, LocalDateTime deliveredAt);

}
