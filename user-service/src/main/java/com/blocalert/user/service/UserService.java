package com.blocalert.user.service;

import com.blocalert.dto.UserContactInfo;
import com.blocalert.user.dto.request.NewUserRequest;
import com.blocalert.user.dto.request.UpdateUserRequest;
import com.blocalert.user.dto.response.UserResponse;
import com.blocalert.user.entity.User;
import java.util.Map;
import java.util.Set;

public interface UserService {

    void createUser(NewUserRequest userRequest);

    Long getUserIdByKeycloakId(String keycloakId);

    UserResponse getUserDetails(Long userId);

    User getUser(Long userId);

    User getUserByStripeCustomerId(String customerId);

    void updateUser(Long userId, UpdateUserRequest request);

    void updateCustomerId(Long userId, String customerId);

    Map<Long, UserContactInfo> getUsersContactInfo(Set<Long> userIds);

    void upgradeToPremium(Long userId);

    void downgradeUserToFree(Long id);

}
