package com.example.keycloak;

import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.UriUtils;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.CommonClientSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

final class PasskeyBrowserLoginService {

    private static final String HEADER_ORIGIN = "Origin";
    private static final String HEADER_REFERER = "Referer";
    private static final String RESPONSE_MODE_QUERY = "query";

    private final KeycloakSession session;
    private final PasskeyClientSupport clientSupport;

    /**
     * Creates a helper that continues the standard Keycloak browser login flow.
     *
     * @param session Keycloak request session
     * @param clientSupport helper for resolving configured client
     */
    PasskeyBrowserLoginService(KeycloakSession session, PasskeyClientSupport clientSupport) {
        this.session = session;
        this.clientSupport = clientSupport;
    }

    /**
     * Builds an authenticated browser flow response for a passkey-authenticated user.
     *
     * @param user authenticated user
     * @param realm current realm
     * @return Keycloak follow-up response from the authentication manager
     */
    Response completeLogin(UserModel user, RealmModel realm) {
        ClientModel client = clientSupport.requireConfiguredClient(realm);
        String redirectUri = resolveValidatedRedirectUri(client);
        if (redirectUri == null) {
            throw new IllegalArgumentException("No valid redirect URI for client");
        }

        session.getContext().setClient(client);
        AuthenticationSessionModel authenticationSession = createBrowserAuthenticationSession(realm, client, user, redirectUri);
        session.getContext().setAuthenticationSession(authenticationSession);

        ClientConnection connection = session.getContext().getConnection();
        EventBuilder event = new EventBuilder(realm, session, connection)
                .event(EventType.LOGIN)
                .client(client)
                .user(user);
        return AuthenticationManager.nextActionAfterAuthentication(
                session,
                authenticationSession,
                connection,
                session.getContext().getHttpRequest(),
                session.getContext().getUri(),
                event
        );
    }

    /**
     * Creates and pre-populates an authentication session for OIDC browser continuation.
     */
    private AuthenticationSessionModel createBrowserAuthenticationSession(RealmModel realm, ClientModel client, UserModel user, String redirectUri) {
        AuthenticationSessionManager authenticationSessionManager = new AuthenticationSessionManager(session);
        RootAuthenticationSessionModel rootAuthenticationSession = authenticationSessionManager.createAuthenticationSession(realm, true);
        AuthenticationSessionModel authenticationSession = rootAuthenticationSession.createAuthenticationSession(client);

        authenticationSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authenticationSession.setAction(CommonClientSessionModel.Action.AUTHENTICATE.name());
        authenticationSession.setAuthenticatedUser(user);
        authenticationSession.setRedirectUri(redirectUri);
        authenticationSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, OIDCResponseType.NONE);
        authenticationSession.setClientNote(OIDCLoginProtocol.RESPONSE_MODE_PARAM, RESPONSE_MODE_QUERY);
        authenticationSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
        authenticationSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, resolveScopeParameter(client));
        AuthenticationManager.setClientScopesInSession(session, authenticationSession);
        authenticationSession.setClientNote(OIDCLoginProtocol.STATE_PARAM, generateState());
        authenticationSession.setClientNote(
                OIDCLoginProtocol.ISSUER,
                Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName())
        );
        return authenticationSession;
    }

    /**
     * Derives scope to place into the auth session; falls back to {@code openid}.
     */
    private String resolveScopeParameter(ClientModel client) {
        return PasskeyConfigResolver.firstNonBlank(
                AuthenticationManager.getRequestedScopes(session, client),
                OAuth2Constants.SCOPE_OPENID
        );
    }

    /**
     * Generates a random state value used by the browser flow.
     */
    private String generateState() {
        Challenge challenge = new DefaultChallenge();
        return Base64Url.encode(challenge.getValue());
    }

    /**
     * Finds the first client-validated redirect URI from request hints and configured fallbacks.
     */
    private String resolveValidatedRedirectUri(ClientModel client) {
        List<String> redirectCandidates = new ArrayList<>();
        redirectCandidates.add(normalizeRedirectCandidate(getHeaderValue(HEADER_REFERER), true));
        redirectCandidates.add(normalizeRedirectCandidate(getHeaderValue(HEADER_ORIGIN), false));
        redirectCandidates.add(normalizeRedirectCandidate(client.getBaseUrl(), true));

        for (String redirectCandidate : redirectCandidates) {
            if (redirectCandidate == null) {
                continue;
            }
            String verifiedRedirectUri = RedirectUtils.verifyRedirectUri(session, redirectCandidate, client);
            if (verifiedRedirectUri != null) {
                return verifiedRedirectUri;
            }
        }

        String singleConfiguredRedirect = RedirectUtils.verifyRedirectUri(session, null, client, false);
        if (singleConfiguredRedirect != null) {
            return singleConfiguredRedirect;
        }

        for (String configuredRedirectUri : client.getRedirectUris()) {
            if (configuredRedirectUri == null || configuredRedirectUri.isBlank() || configuredRedirectUri.contains("*")) {
                continue;
            }
            String verifiedRedirectUri = RedirectUtils.verifyRedirectUri(session, configuredRedirectUri, client);
            if (verifiedRedirectUri != null) {
                return verifiedRedirectUri;
            }
        }

        return null;
    }

    /**
     * Reads a header value from the current request.
     */
    private String getHeaderValue(String headerName) {
        var headers = session.getContext().getRequestHeaders();
        if (headers == null) {
            return null;
        }
        return headers.getHeaderString(headerName);
    }

    /**
     * Normalizes a redirect candidate to a stable URI form before validation.
     */
    private String normalizeRedirectCandidate(String rawUri, boolean preservePath) {
        if (rawUri == null || rawUri.isBlank()) {
            return null;
        }

        try {
            URI parsedUri = URI.create(rawUri.trim());
            if (parsedUri.getScheme() == null || parsedUri.getHost() == null) {
                return null;
            }

            if (!preservePath) {
                return UriUtils.getOrigin(parsedUri);
            }

            String path = parsedUri.getPath();
            if (path == null || path.isBlank()) {
                path = "/";
            }

            URI normalizedUri = new URI(
                    parsedUri.getScheme(),
                    parsedUri.getUserInfo(),
                    parsedUri.getHost(),
                    parsedUri.getPort(),
                    path,
                    null,
                    null
            );
            return normalizedUri.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

}
