package com.blocalert.keycloak.dto;

public record UserRequest(
   String keycloakId,
   String email,
   String name,
   long createdAt
){}