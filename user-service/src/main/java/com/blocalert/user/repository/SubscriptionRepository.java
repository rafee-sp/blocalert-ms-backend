package com.blocalert.user.repository;

import com.blocalert.user.dto.response.SubscriptionDetailResponse;
import com.blocalert.user.entity.Subscription;
import com.blocalert.user.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByCorrelationId(String correlationId);

    Optional<Subscription> findByUser_IdAndSessionId(Long userId, String sessionId);

    Optional<Subscription> findByUser_IdAndSubscriptionId(Long id, String subscriptionId);

    Optional<Subscription> findByUser_IdAndSubscriptionStatus(Long id, SubscriptionStatus subscriptionStatus);

    @Query("SELECT new com.blocalert.user.dto.response.SubscriptionDetailResponse(s.id, s.subscriptionStatus, s.currentSubscriptionEnd) " +
            "FROM Subscription s WHERE s.user.id = ?1 AND s.subscriptionStatus = ACTIVE ORDER BY s.createdAt DESC")
    Optional<SubscriptionDetailResponse> getRecentUserSubscription(Long userId);

}
