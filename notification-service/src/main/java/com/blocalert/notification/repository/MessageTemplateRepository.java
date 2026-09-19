package com.blocalert.notification.repository;

import com.blocalert.enums.AlertChannel;
import com.blocalert.notification.entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    Optional<MessageTemplate> findByChannelAndCode(AlertChannel channel, String templateCode);
}
