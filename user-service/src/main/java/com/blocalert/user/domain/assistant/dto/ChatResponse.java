package com.blocalert.user.domain.assistant.dto;

public record ChatResponse(
        String response
) {

    public static ChatResponse of(String response) {
        return new ChatResponse(response);
    }

}
