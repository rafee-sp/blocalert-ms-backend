package com.blocalert.alert.service;

public interface UserService {

    Long getCurrentUserId();

    Long getUserId(String keycloakId);
}
