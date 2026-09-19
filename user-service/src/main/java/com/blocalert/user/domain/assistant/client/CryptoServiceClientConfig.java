package com.blocalert.user.domain.assistant.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class CryptoServiceClientConfig {

    @Bean
    CryptoServiceClient cryptoServiceClient(RestClient.Builder builder,
                                        OAuth2AuthorizedClientManager auth2AuthorizedClientManager,
                                        @Value("${services.crypto-service.base-url}") String baseUrl) {

        OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(auth2AuthorizedClientManager);

        interceptor.setClientRegistrationIdResolver(_ -> "user-service-client");

        RestClient restClient = builder
                .baseUrl(baseUrl)
                .requestInterceptor(interceptor)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(CryptoServiceClient.class);
    }
}
