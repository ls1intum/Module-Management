package com.example.keycloak;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class UserPasskeyProvider implements RealmResourceProvider {

    private final KeycloakSession session;
    private final String allowedBrowserOriginPattern;
    private final String clientId;

    public UserPasskeyProvider(KeycloakSession session, String allowedBrowserOriginPattern, String clientId) {
        this.session = session;
        this.allowedBrowserOriginPattern = allowedBrowserOriginPattern;
        this.clientId = clientId;
    }

    @Override
    public Object getResource() {
        return new UserPasskeyResource(session, allowedBrowserOriginPattern, clientId);
    }

    @Override
    public void close() {
    }
}
