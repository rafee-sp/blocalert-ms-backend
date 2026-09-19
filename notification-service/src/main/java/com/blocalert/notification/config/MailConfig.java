package com.blocalert.notification.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "mail")
@Validated
@Data
public class MailConfig {

    @NotBlank(message = "Mail source must be valid")
    private String from;

    @NotBlank(message = "Mail sender name must be valid")
    private String name;

    @NotNull(message = "Mail rate limit value is not valid")
    @Positive(message = "Mail rate limit value is not valid")
    private Long rateLimit;
}
