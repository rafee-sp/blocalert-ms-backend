package com.blocalert.keycloak.events;

import com.blocalert.keycloak.client.EventClient;
import com.blocalert.keycloak.client.TokenClient;
import com.blocalert.keycloak.config.EventConfig;
import com.blocalert.keycloak.service.UserService;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UserEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOG =
            Logger.getLogger(UserEventListenerProviderFactory.class);

    private UserService userService;

    @Override
    public EventListenerProvider create(KeycloakSession session) {

        return new UserEventListenerProvider(session, userService);
    }

    @Override
    public void init(Config.Scope config) {

        LOG.info("Initializing User Provision Event Listener");

        EventConfig eventConfig =
                EventConfig.fromConfig();

        TokenClient tokenClient =
                new TokenClient(eventConfig);

        EventClient eventClient =new EventClient(eventConfig);

        userService = new UserService(tokenClient, eventClient);

        LOG.info("User Provision Event Listener initialized");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing to initialize after Keycloak startup
    }

    @Override
    public void close() {
        LOG.info("Shutting down User Provision Event Listener");
    }

    @Override
    public String getId() {
        return "user-event";
    }
}