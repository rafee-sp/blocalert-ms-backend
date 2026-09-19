package com.blocalert.keycloak.config;

public record EventConfig (
   String keycloakBaseUrl,
   String realm,
   String clientId,
   String clientSecret,
   String userServiceUrl
){

    public static EventConfig fromConfig() {
        return new EventConfig(
                validate("KC_BASE_URL"),
                validate("KC_REALM"),
                validate("KC_EVENT_CLIENT_ID"),
                validate("KC_EVENT_CLIENT_SECRET"),
                validate("USER_SERVICE_URL")
        );

    }

    private static String validate(String key) {

        String value = System.getenv(key);

        if (value == null || value.isBlank())
            throw new IllegalStateException("Missing environment variable " + key);

        return value;

    }

}
