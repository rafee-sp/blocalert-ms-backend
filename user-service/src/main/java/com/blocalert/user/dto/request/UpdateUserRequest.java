package com.blocalert.user.dto.request;

import com.blocalert.user.entity.enums.Theme;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    private Theme theme;

}
