package com.blocalert.user.service.impl;

import com.blocalert.dto.UserContactInfo;
import com.blocalert.enums.UserRole;
import com.blocalert.user.dto.request.NewUserRequest;
import com.blocalert.user.dto.request.UpdateUserRequest;
import com.blocalert.user.dto.response.UserResponse;
import com.blocalert.user.entity.User;
import com.blocalert.user.exception.ResourceNotFoundException;
import com.blocalert.user.repository.UserRepository;
import com.blocalert.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    @Override
    @Transactional
    public void createUser(NewUserRequest userRequest) {

        log.info("createUser called");

        boolean isUserExists = userRepository.existsByKeycloakId(userRequest.keycloakId());

        if(isUserExists)
            throw new IllegalStateException("User already present with the KeycloakId : " + userRequest.keycloakId());

        User newUser = new User();
        newUser.setKeycloakId(userRequest.keycloakId());
        newUser.setEmail(userRequest.email());
        if(StringUtils.hasText(userRequest.name())){
            newUser.setName(userRequest.name());
        }

        userRepository.save(newUser);
    }

    @Override
    public Long getUserIdByKeycloakId(String keycloakId) {

        log.debug("getUserIdByKeycloakId called for {}", keycloakId);

        if(!StringUtils.hasText(keycloakId))
            throw new IllegalArgumentException("keycloakId is not valid " + keycloakId);

        User user =  userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloakId : "+keycloakId));

        return user.getId();
    }

    @Override
    public UserResponse getUserDetails(Long userId) {

        log.debug("getUserDetails called for {}", userId);

        if(userId == null || userId <= 0) throw new IllegalArgumentException("User Id cannot be invalid");

        return userRepository.getUserDetails(userId).
                orElseThrow(() -> new ResourceNotFoundException("User not found with id : "+ userId));
    }

    @Override
    public User getUserByStripeCustomerId(String customerId) {
       return userRepository.findByStripeCustomerId(customerId)
               .orElseThrow(() -> new ResourceNotFoundException("User not found with stripe customer Id  "+ customerId));
    }

    @Override
    @Transactional
    public void updateCustomerId(Long userId, String customerId) {
        User user = getUser(userId);
        user.setStripeCustomerId(customerId);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(Long userId, UpdateUserRequest request) {
        log.info("updateUser called for userId : {}",userId);
        User user = getUser(userId);

        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getTheme() != null) {
            user.setThemePreference(request.getTheme());
        }

        userRepository.save(user);
    }


    @Override
    public Map<Long, UserContactInfo> getUsersContactInfo(Set<Long> userIds) {

        return userRepository.findContactInfoByIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserContactInfo::id, Function.identity()));
    }

    @Override
    @Transactional
    public void upgradeToPremium(Long userId) {

        log.info("upgradeToPremium called");
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with Id : "+userId));
        user.setSubscribed(true);
        user.setRole(UserRole.ROLE_PREMIUM_USER);
        userRepository.save((user));

        keycloakService.updateRole(user.getKeycloakId(), UserRole.ROLE_PREMIUM_USER.getRole(), UserRole.ROLE_FREE_USER.getRole());
    }

    @Override
    @Transactional
    public void downgradeUserToFree(Long userId) {

        log.info("downgradeUserToFree called");
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found with Id : "+userId));
        user.setSubscribed(false);
        user.setRole(UserRole.ROLE_FREE_USER);
        userRepository.save(user);

        keycloakService.updateRole(user.getKeycloakId(), UserRole.ROLE_FREE_USER.getRole(), UserRole.ROLE_PREMIUM_USER.getRole());
    }

    @Override
    public User getUser(Long userId) {
        if(userId == null || userId <= 0) throw new IllegalArgumentException("User Id cannot be invalid");
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id : "+userId));
    }
}
