package com.example.keycloak;

import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import org.keycloak.common.util.Base64Url;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.util.Objects;

final class PasskeyChallengeService {

    private static final long CHALLENGE_TTL_MILLIS = 120_000;
    private static final String AUTH_NOTE_PASSKEY_CHALLENGE = "passkey.challenge";
    private static final String AUTH_NOTE_PASSKEY_CHALLENGE_ISSUED_AT = "passkey.challenge.issuedAt";

    private final KeycloakSession session;
    private final PasskeyClientSupport clientSupport;

    /**
     * Creates a challenge service bound to the current request session.
     *
     * @param session Keycloak request session
     * @param clientSupport helper for resolving configured client
     */
    PasskeyChallengeService(KeycloakSession session, PasskeyClientSupport clientSupport) {
        this.session = session;
        this.clientSupport = clientSupport;
    }

    /**
     * Generates and stores a single-use passkey challenge in authentication-session notes.
     *
     * @return base64url encoded challenge value
     */
    String issueChallenge() {
        RealmModel realm = requireRealm();
        ClientModel client = clientSupport.requireConfiguredClient(realm);
        AuthenticationSessionModel challengeSession = getOrCreateChallengeSession(realm, client);
        String challenge = generateChallenge();
        challengeSession.setAuthNote(AUTH_NOTE_PASSKEY_CHALLENGE, challenge);
        challengeSession.setAuthNote(AUTH_NOTE_PASSKEY_CHALLENGE_ISSUED_AT, Long.toString(System.currentTimeMillis()));
        return challenge;
    }

    /**
     * Consumes a challenge if it matches and is still within the configured TTL.
     *
     * @param challenge challenge sent by client
     * @return {@code true} when challenge exists, matches, and is not expired
     */
    boolean consumeChallenge(String challenge) {
        if (challenge == null || challenge.isBlank()) {
            return false;
        }

        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            return false;
        }

        ClientModel client = clientSupport.resolveConfiguredClient(realm);
        if (client == null) {
            return false;
        }

        AuthenticationSessionModel challengeSession = resolveChallengeSession(realm, client);
        if (challengeSession == null) {
            return false;
        }

        String trackedChallenge = challengeSession.getAuthNote(AUTH_NOTE_PASSKEY_CHALLENGE);
        if (!Objects.equals(challenge, trackedChallenge)) {
            return false;
        }

        challengeSession.removeAuthNote(AUTH_NOTE_PASSKEY_CHALLENGE);
        String issuedAtRaw = challengeSession.getAuthNote(AUTH_NOTE_PASSKEY_CHALLENGE_ISSUED_AT);
        challengeSession.removeAuthNote(AUTH_NOTE_PASSKEY_CHALLENGE_ISSUED_AT);
        return isWithinTtl(issuedAtRaw);
    }

    /**
     * Returns the current realm or throws when realm context is missing.
     */
    private RealmModel requireRealm() {
        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            throw new IllegalStateException("Realm context is unavailable");
        }
        return realm;
    }

    /**
     * Verifies whether the stored issue timestamp is still valid.
     */
    private boolean isWithinTtl(String issuedAtRaw) {
        try {
            long issuedAt = Long.parseLong(PasskeyConfigResolver.firstNonBlank(issuedAtRaw, "0"));
            return issuedAt > 0 && (System.currentTimeMillis() - issuedAt) <= CHALLENGE_TTL_MILLIS;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Produces a random base64url challenge string.
     */
    private String generateChallenge() {
        Challenge challenge = new DefaultChallenge();
        return Base64Url.encode(challenge.getValue());
    }

    /**
     * Returns an existing challenge session for the client, or creates a new one.
     */
    private AuthenticationSessionModel getOrCreateChallengeSession(RealmModel realm, ClientModel client) {
        AuthenticationSessionModel existingSession = resolveChallengeSession(realm, client);
        if (existingSession != null) {
            return existingSession;
        }

        AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(session);
        RootAuthenticationSessionModel rootAuthenticationSession = authenticationSessionManager.createAuthenticationSession(realm, true);
        AuthenticationSessionModel authenticationSession = rootAuthenticationSession.createAuthenticationSession(client);
        authenticationSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        return authenticationSession;
    }

    /**
     * Resolves the challenge session associated with the configured client.
     */
    private AuthenticationSessionModel resolveChallengeSession(RealmModel realm, ClientModel client) {
        AuthenticationSessionModel contextAuthenticationSession = session.getContext().getAuthenticationSession();
        if (isSessionForClient(contextAuthenticationSession, client)) {
            return contextAuthenticationSession;
        }

        AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(session);
        RootAuthenticationSessionModel rootAuthenticationSession = authenticationSessionManager.getCurrentRootAuthenticationSession(realm);
        if (rootAuthenticationSession == null) {
            return null;
        }

        return rootAuthenticationSession.getAuthenticationSessions().values().stream()
                .filter(authenticationSession -> isSessionForClient(authenticationSession, client))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks whether an authentication session belongs to the given client.
     */
    private boolean isSessionForClient(AuthenticationSessionModel authenticationSession, ClientModel client) {
        return authenticationSession != null
                && client != null
                && authenticationSession.getClient() != null
                && Objects.equals(authenticationSession.getClient().getId(), client.getId());
    }
}
