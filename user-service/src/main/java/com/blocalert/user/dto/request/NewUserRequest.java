package com.blocalert.user.dto.request;

public record NewUserRequest(
   String keycloakId,
   String email,
   String name,
   long createdAt
){}