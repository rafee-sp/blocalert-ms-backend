package com.blocalert.user.controller;

import com.blocalert.dto.UserContactInfo;
import com.blocalert.dto.ApiResponse;
import com.blocalert.dto.UserIdResponse;
import com.blocalert.user.dto.request.NewUserRequest;
import com.blocalert.user.dto.request.UpdateUserRequest;
import com.blocalert.user.dto.response.UserResponse;
import com.blocalert.user.service.UserService;
import com.blocalert.user.service.impl.CurrentUserProvider;
import com.blocalert.user.service.impl.KeycloakService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider userProvider;
    private final KeycloakService service;

    @PostMapping("/register")
    public ResponseEntity<Void> addNewUser(@RequestBody NewUserRequest userRequest) {

        userService.createUser(userRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getLoggedInUser() {
        log.info("UserController(getLoggedInUser) called");
        Long userId = userProvider.getCurrentUserId();
        UserResponse user = userService.getUserDetails(userId);
        return ResponseEntity.ok().body(new ApiResponse("User fetched", user));
    }

    @PatchMapping
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest request) {
        Long userId = userProvider.getCurrentUserId();
        userService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/local-id")
    public UserIdResponse getUserId(@RequestParam
                                    @NotBlank(message = "KeycloakId must be valid")
                                    String keycloakId) {

        return new UserIdResponse(userService.getUserIdByKeycloakId(keycloakId));
    }

    @GetMapping("/contact-info")
    public Map<Long, UserContactInfo> getContactInfo(@RequestParam("ids") Set<Long> userIds){
        return userService.getUsersContactInfo(userIds);
    }

}
