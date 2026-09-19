package com.blocalert.keycloak.service;

import com.blocalert.keycloak.client.EventClient;
import com.blocalert.keycloak.client.TokenClient;
import com.blocalert.keycloak.dto.UserRequest;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    private final TokenClient tokenClient;
    private final EventClient eventClient;

    public UserService(TokenClient tokenClient, EventClient eventClient) {
        this.tokenClient = tokenClient;
        this.eventClient = eventClient;
    }

    public void handleEvent(Event event, KeycloakSession session){

        RealmModel realm = session.realms().getRealm(event.getRealmId());

        UserModel user = session.users().getUserById(realm, event.getUserId());

        if (user == null) {
            LOG.warn("User not found with Id "+ event.getUserId());
            return;
        }

        LOG.infov("User registered: {0} {1} {2} {3} {4}",
                user.getUsername(),
                user.getEmail(),
                user.getCreatedTimestamp(),
                user.getFirstName(),
                user.getLastName());

        UserRequest userRequest = new UserRequest(
                user.getId(),
                user.getEmail(),
                user.getFirstName()+" "+ user.getLastName(),
                user.getCreatedTimestamp()
        );

        String accessToken = tokenClient.getAccessToken();

        try {
            eventClient.sendEvent(userRequest, accessToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
