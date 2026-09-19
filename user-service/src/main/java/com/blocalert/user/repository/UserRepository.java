package com.blocalert.user.repository;

import com.blocalert.dto.UserContactInfo;
import com.blocalert.user.dto.response.UserResponse;
import com.blocalert.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByKeycloakId(String keycloakId);

    @Query("SELECT new com.blocalert.user.dto.response.UserResponse(u.name, u.email, u.phoneNumber, u.role, u.themePreference) FROM User u WHERE u.id = ?1")
    Optional<UserResponse> getUserDetails(Long userId);

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByStripeCustomerId(String customerId);

    @Query("SELECT new com.blocalert.dto.UserContactInfo(u.id, u.email, u.phoneNumber, u.isSubscribed) FROM User u WHERE u.id IN ?1")
    List<UserContactInfo> findContactInfoByIds(Set<Long> userIds);
}
