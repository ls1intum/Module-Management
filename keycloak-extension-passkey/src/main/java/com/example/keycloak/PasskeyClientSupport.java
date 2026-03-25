package com.example.keycloak;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;

final class PasskeyClientSupport {

    private final String clientId;

    /**
     * Creates a helper for resolving the configured passkey client.
     *
     * @param clientId configured client identifier
     */
    PasskeyClientSupport(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Resolves the configured client in the given realm.
     *
     * @param realm current realm
     * @return matching client, or {@code null} when configuration/context is incomplete
     */
    ClientModel resolveConfiguredClient(RealmModel realm) {
        if (realm == null || clientId == null || clientId.isBlank()) {
            return null;
        }
        return realm.getClientByClientId(clientId);
    }

    /**
     * Resolves the configured client and throws when it cannot be found.
     *
     * @param realm current realm
     * @return configured client model
     */
    ClientModel requireConfiguredClient(RealmModel realm) {
        ClientModel client = resolveConfiguredClient(realm);
        if (client == null) {
            throw new IllegalStateException("Client not found for configured passkey client_id");
        }
        return client;
    }
}
