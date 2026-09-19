package com.blocalert.dto;

public record UserContactInfo(Long id, String email, String phoneNumber, boolean isSubscribed) {
}
