package com.blocalert.keycloak.events;

import com.blocalert.keycloak.service.UserService;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class UserEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;
    private final UserService userService;

    public UserEventListenerProvider(KeycloakSession session, UserService userService) {
        this.session = session;
        this.userService = userService;
    }

    @Override
    public void onEvent(Event event) {

        if (event.getType() != EventType.REGISTER) {
            return;
        }

        userService.handleEvent(event, session);
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {

    }

    @Override
    public void close() {

    }
}