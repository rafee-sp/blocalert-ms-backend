package com.blocalert.user.repository;

import com.blocalert.user.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    boolean existsByEventId(String id);
}
