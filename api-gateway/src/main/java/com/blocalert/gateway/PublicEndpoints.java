package com.blocalert.gateway;

public class PublicEndpoints {

    private PublicEndpoints(){};

    public static final String[] PATHS = {
            "/actuator/**",
            "/sms/callback/**",
            "/ws/home/**",
            "/ws/crypto-detail/**",
            "/ws/alerts/**",
            "/api/cryptos/search",
            "/api/stripe/callback",
    };

}
