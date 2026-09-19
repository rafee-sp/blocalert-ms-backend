package com.blocalert.user.service.impl;

import com.blocalert.user.config.KeycloakAdminProperties;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final Keycloak keycloak;
    private final KeycloakAdminProperties props;

    @Retry(name = "keycloakAdminCB")
    public void updateRole(String keycloakId, String newRole, String oldRole) {

        log.info("updateRole called for {} , role {} to {}", keycloakId, oldRole, newRole);

        RealmResource realm = keycloak.realm(props.realm());
        UserResource user = realm.users().get(keycloakId);

        RoleRepresentation newRoleRep = realm.roles().get(newRole).toRepresentation();
        user.roles().realmLevel().add(List.of(newRoleRep));
        log.info("New role updated");

        if (oldRole != null) {
            RoleRepresentation oldRoleRepo = realm.roles().get(oldRole).toRepresentation();
            user.roles().realmLevel().remove(List.of(oldRoleRepo));
            log.info("Old role removed");
        }
    }
}
