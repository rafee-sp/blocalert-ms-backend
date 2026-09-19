package com.blocalert.notification.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "application")
@Validated
@Data
public class AppConfig {

    @NotBlank(message = "Frontend URL must not be blank")
    private String frontendUrl;

    @NotBlank(message = "Backend URL must not be blank")
    private String backendUrl;
}