package com.example.keycloak;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class UserPasskeyProviderFactory implements RealmResourceProviderFactory {

    public static final String ID = "passkey";
    private String allowedBrowserOriginPattern;
    private String clientId;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new UserPasskeyProvider(session, allowedBrowserOriginPattern, clientId);
    }

    @Override
    public void init(Config.Scope config) {
        allowedBrowserOriginPattern = PasskeyConfigResolver.resolveAllowedOriginPattern(config);
        clientId = PasskeyConfigResolver.resolveClientId(config);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

}
