package com.blocalert.user.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakAdminProperties.class)
public class KeycloakAdminConfig {

    @Bean
    public Keycloak keycloak(KeycloakAdminProperties props) {
        return KeycloakBuilder.builder()
                .serverUrl(props.serverUrl())
                .realm(props.realm())
                .clientId(props.clientId())
                .clientSecret(props.clientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
