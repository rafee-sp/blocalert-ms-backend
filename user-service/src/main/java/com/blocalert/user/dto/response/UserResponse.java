package com.blocalert.user.dto.response;

import com.blocalert.enums.UserRole;
import com.blocalert.user.entity.enums.Theme;

public record UserResponse(
        String name,
        String email,
        String phoneNumber,
        UserRole role,
        Theme themePreference
) {
}
