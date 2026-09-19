package com.blocalert.user.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "keycloak.admin")
@Validated
public record KeycloakAdminProperties(

        @NotBlank(message = "Keycloak admin server URL must not be blank")
        String serverUrl,

        @NotBlank(message = "Keycloak admin realm must not be blank")
        String realm,

        @NotBlank(message = "Keycloak admin client ID must not be blank")
        String clientId,

        @NotBlank(message = "Keycloak admin client secret must not be blank")
        String clientSecret
) {
}