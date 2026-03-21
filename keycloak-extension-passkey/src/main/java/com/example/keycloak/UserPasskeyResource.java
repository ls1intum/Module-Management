package com.example.keycloak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.WebAuthnRegistrationManager;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.common.util.Base64Url;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.WebAuthnCredentialModelInput;
import org.keycloak.credential.WebAuthnCredentialProvider;
import org.keycloak.credential.WebAuthnPasswordlessCredentialProviderFactory;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.RefreshToken;
import org.keycloak.services.util.DefaultClientSessionContext;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Path("/")
public class UserPasskeyResource {

    private final KeycloakSession session;
    private static final Logger logger = Logger.getLogger(UserPasskeyResource.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final long CHALLENGE_TTL_MILLIS = 2 * 60 * 1000;
    private static final ConcurrentHashMap<String, Long> ISSUED_CHALLENGES = new ConcurrentHashMap<>();
    private static final String CREDENTIAL_USER_ATTR = "passkey-credential-id";

    /**
     * Custom /passkey/* routes do not get Keycloak's OIDC CORS (Web origins). Echo Origin only when it matches.
     * Localhost/loopback + staging app host (see module-management client webOrigins).
     */
    private static final Pattern ALLOWED_BROWSER_ORIGIN = Pattern.compile(
            "^https?://(localhost|127\\.0\\.0\\.1|\\[::1\\])(:\\d+)?$|^https://module\\.aet\\.cit\\.tum\\.de$");

    @Inject
    public UserPasskeyResource(KeycloakSession session) {
        this.session = session;
    }

    private String getOriginHeader() {
        var headers = session.getContext().getRequestHeaders();
        if (headers == null) {
            return null;
        }
        return headers.getHeaderString("Origin");
    }

    private boolean isAllowedOrigin(String origin) {
        return origin != null && ALLOWED_BROWSER_ORIGIN.matcher(origin.trim()).matches();
    }

    private Response withCors(Response response) {
        String origin = getOriginHeader();
        if (origin == null || !isAllowedOrigin(origin)) {
            return response;
        }
        return Response.fromResponse(response)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", "true")
                .header("Vary", "Origin")
                .build();
    }

    @OPTIONS
    @Path("{any:.*}")
    public Response corsPreflight() {
        Response.ResponseBuilder b = Response.ok()
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept")
                .header("Access-Control-Max-Age", "3600");
        return withCors(b.build());
    }

    @GET
    @Path("challenge")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChallenge() {
        String challengeBase64 = generateChallenge();
        trackChallenge(challengeBase64);

        return withCors(Response.ok("{\"challenge\": \"" + challengeBase64 + "\"}")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build());
    }

    @GET
    @Path("/get-credential-id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCredentialId(@QueryParam("username") String username) {
        String challengeBase64 = generateChallenge();
        trackChallenge(challengeBase64);

        if (username == null || username.isBlank()) {
            return withCors(Response.ok("{\"challenge\": \"" + challengeBase64 + "\"}")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build());
        }

        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserByUsername(realm, username);

        if (user == null) {
            return withCors(Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"User not found\"}")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build());
        }

        List<CredentialModel> webAuthnCredentials = user.credentialManager()
                .getStoredCredentialsStream()
                .filter(cred -> WebAuthnCredentialModel.TYPE_PASSWORDLESS.equals(cred.getType()))
                .toList();

        if (webAuthnCredentials.isEmpty()) {
            return withCors(Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"No passkey found for user\"}")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build());
        }

        WebAuthnCredentialModel credentialModel = WebAuthnCredentialModel.createFromCredentialModel(webAuthnCredentials.get(0));
        String credentialIdBase64 = credentialModel.getWebAuthnCredentialData().getCredentialId();
        String jsonResponse = "{\"credentialId\": \"" + credentialIdBase64 + "\", \"challenge\": \"" + challengeBase64 + "\"}";

        return withCors(Response.ok(jsonResponse)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build());
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response savePasskey(PasskeyRequest request, @HeaderParam("Authorization") String authorizationHeader) throws JsonProcessingException {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = getUserFromBearerToken(realm, authorizationHeader);
        if (user == null) {
            return withCors(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Authenticated user not found from access token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                    .build());
        }

        String base64ClientDataJSON = request.getClientDataJSON();

        byte[] decodedBytes = decodeBase64UrlOrStd(base64ClientDataJSON);
        String decodedClientDataJSON = new String(decodedBytes, StandardCharsets.UTF_8);
        JsonNode clientData = OBJECT_MAPPER.readTree(decodedClientDataJSON);

        Origin origin = new Origin(clientData.get("origin").asText());
        String rpId = clientData.get("origin").asText().replace("http://", "").replace("https://", "").split(":")[0];

        Challenge challenge = new DefaultChallenge(clientData.get("challenge").asText());

        Set<Origin> originSet = new HashSet<>();
        originSet.add(origin);
        ServerProperty serverProperty = new ServerProperty(originSet, rpId, challenge, null);
        RegistrationParameters registrationParameters = new RegistrationParameters(serverProperty, true);

        byte[] attestationObject = decodeBase64UrlOrStd(request.getAttestationObject());
        byte[] clientDataJSON = decodeBase64UrlOrStd(request.getClientDataJSON());
        RegistrationRequest registrationRequest = new RegistrationRequest(attestationObject, clientDataJSON);

        WebAuthnRegistrationManager webAuthnRegistrationManager = createWebAuthnRegistrationManager();
        RegistrationData registrationData = webAuthnRegistrationManager.parse(registrationRequest);
        validateRegistrationCompat(webAuthnRegistrationManager, registrationRequest, registrationData, registrationParameters);

        WebAuthnCredentialModelInput credential = new WebAuthnCredentialModelInput(WebAuthnCredentialModel.TYPE_PASSWORDLESS);
        credential.setAttestedCredentialData(registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData());
        credential.setCount(registrationData.getAttestationObject().getAuthenticatorData().getSignCount());
        credential.setAttestationStatementFormat(registrationData.getAttestationObject().getFormat());
        credential.setTransports(registrationData.getTransports());

        WebAuthnCredentialProvider webAuthnCredProvider = (WebAuthnCredentialProvider) this.session.getProvider(CredentialProvider.class, WebAuthnPasswordlessCredentialProviderFactory.PROVIDER_ID);
        WebAuthnCredentialModel credentialModel = webAuthnCredProvider.getCredentialModelFromCredentialInput(credential, user.getUsername());

        WebAuthnCredentialModel webAuthnCredentialModel = WebAuthnCredentialModel.createFromCredentialModel(credentialModel);

        user.credentialManager().createStoredCredential(webAuthnCredentialModel);
        storeCredentialUserMapping(user, webAuthnCredentialModel.getWebAuthnCredentialData().getCredentialId());

        return withCors(Response.status(Response.Status.CREATED)
                .entity("Passkey stored successfully")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .build());
    }

    private UserModel getUserFromBearerToken(RealmModel realm, String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }

        AccessToken accessToken = session.tokens().decode(token, AccessToken.class);
        if (accessToken == null || accessToken.getSubject() == null || accessToken.getSubject().isBlank()) {
            return null;
        }

        return session.users().getUserById(realm, accessToken.getSubject());
    }

    @POST
    @Path("authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticatePasskey(PasskeyRequest request) throws JsonProcessingException {
        RealmModel realm = session.getContext().getRealm();

        String requestCredentialId = firstNonBlank(request.getCredentialId(), request.getRawId());
        if (requestCredentialId == null) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "credentialId or rawId is required");
        }

        if (!consumeChallenge(request.getChallenge())) {
            return buildErrorResponse(Response.Status.UNAUTHORIZED, "Invalid or expired challenge");
        }

        UserModel user = getUserByCredentialId(realm, requestCredentialId);

        if (user == null)
            return buildErrorResponse(
                    Response.Status.NOT_FOUND,
                    "User not found for credential (username: <unknown>)"
            );

        WebAuthnCredentialModel webAuthnCredential = getWebAuthnCredential(user, requestCredentialId);
        if (webAuthnCredential == null)
            return buildErrorResponse(Response.Status.NOT_FOUND, "No passkey found for user: " + user.getUsername());

        byte[] credentialId = decodeBase64UrlOrStd(requestCredentialId);
        byte[] authenticatorData = decodeBase64UrlOrStd(request.getAuthenticatorData());
        byte[] signature = Base64Url.decode(request.getSignature());
        String clientDataJSON = request.getClientDataJSON();
        String challenge = request.getChallenge();

        boolean isValid = isPasskeyValid(credentialId, authenticatorData, clientDataJSON, signature, challenge, user, realm);
        if (isValid) {
            return generateTokensResponse(user);
        }
        return buildErrorResponse(Response.Status.UNAUTHORIZED, "Invalid passkey");
    }

    private UserModel getUserByCredentialId(RealmModel realm, String credentialId) {
        String normalizedCredentialId = normalizeCredentialId(credentialId);
        if (normalizedCredentialId == null) {
            return null;
        }

        UserModel mappedUser = session.users()
                .searchForUserByUserAttributeStream(realm, CREDENTIAL_USER_ATTR, normalizedCredentialId)
                .findFirst()
                .orElse(null);
        if (mappedUser != null) {
            return mappedUser;
        }

        int first = 0;
        int maxResults = 200;

        while (true) {
            List<UserModel> users = getUsersPage(realm, first, maxResults);

            if (users.isEmpty()) {
                return null;
            }

            UserModel match = users.stream()
                    .filter(user -> getWebAuthnCredential(user, normalizedCredentialId) != null)
                    .findFirst()
                    .orElse(null);

            if (match != null) {
                return match;
            }

            first += users.size();
            if (users.size() < maxResults) {
                return null;
            }
        }
    }

    private List<UserModel> getUsersPage(RealmModel realm, int first, int maxResults) {
        List<UserModel> users = session.users()
                .searchForUserStream(realm, Map.of("search", ""), first, maxResults)
                .toList();
        if (!users.isEmpty()) {
            return users;
        }

        users = session.users()
                .searchForUserStream(realm, Collections.emptyMap(), first, maxResults)
                .toList();
        if (!users.isEmpty()) {
            return users;
        }

        return session.users()
                .searchForUserStream(realm, "", first, maxResults)
                .toList();
    }

    private WebAuthnCredentialModel getWebAuthnCredential(UserModel user, String credentialId) {
        byte[] requestedCredentialId = credentialIdToBytes(credentialId);
        if (requestedCredentialId.length == 0) {
            return null;
        }

        return user.credentialManager()
                .getStoredCredentialsByTypeStream(WebAuthnCredentialModel.TYPE_PASSWORDLESS)
                .map(WebAuthnCredentialModel::createFromCredentialModel)
                .filter(credential -> {
                    String storedCredentialId = credential.getWebAuthnCredentialData().getCredentialId();
                    if (storedCredentialId == null || storedCredentialId.isBlank()) {
                        return false;
                    }

                    byte[] storedCredentialIdBytes = credentialIdToBytes(storedCredentialId);
                    return storedCredentialIdBytes.length > 0 && java.util.Arrays.equals(requestedCredentialId, storedCredentialIdBytes);
                })
                .findFirst()
                .orElse(null);
    }

    private boolean isPasskeyValid(byte[] credentialId, byte[] authenticatorData, String clientDataJSON, byte[] signature, String challengeRequest, UserModel user, RealmModel realm) throws JsonProcessingException {
        if (challengeRequest == null || challengeRequest.isBlank()) {
            return false;
        }

        byte[] decodedClientDataBytes = decodeBase64UrlOrStd(clientDataJSON);
        String decodedClientDataJSON = new String(decodedClientDataBytes, StandardCharsets.UTF_8);
        JsonNode clientData = OBJECT_MAPPER.readTree(decodedClientDataJSON);

        Origin origin = new Origin(clientData.get("origin").asText());
        String rpId = clientData.get("origin").asText().replace("http://", "").replace("https://", "").split(":")[0];
        Challenge challenge = new DefaultChallenge(challengeRequest);
        ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, null);

        String uvPolicy = realm.getWebAuthnPolicyPasswordless().getUserVerificationRequirement();
        boolean isUVFlagChecked = uvPolicy != null && "required".equalsIgnoreCase(uvPolicy);
        var authReq = new AuthenticationRequest(credentialId, authenticatorData, decodedClientDataBytes, signature);
        var authParams = new WebAuthnCredentialModelInput.KeycloakWebAuthnAuthenticationParameters(serverProperty, isUVFlagChecked);
        var cred = new WebAuthnCredentialModelInput(WebAuthnCredentialModel.TYPE_PASSWORDLESS);

        cred.setAuthenticationRequest(authReq);
        cred.setAuthenticationParameters(authParams);
        return user.credentialManager().isValid(cred);
    }

    private Response generateTokensResponse(UserModel user) {
        try {
            RealmModel realm = session.getContext().getRealm();

            ClientModel client = realm.getClientByClientId("module-management");
            if (client == null) {
                logger.error("Client not found for client_id module-management");
                return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Client not found");
            }
            session.getContext().setClient(client);

            UserSessionModel userSession = session.sessions().createUserSession(
                    realm, user, user.getUsername(), "127.0.0.1", "form", true, null, null);

            AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
            if (clientSession == null) {
                clientSession = session.sessions().createClientSession(realm, client, userSession);
            }

            ClientSessionContext clientSessionCtx = DefaultClientSessionContext.fromClientSessionAndScopeParameter(
                    clientSession, "", session);

            if (session.getContext().getClient() == null) {
                logger.error("Client context is still null after setting.");
                return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Client context is null");
            }

            TokenManager tokenManager = new TokenManager();
            AccessToken accessToken = tokenManager.createClientAccessToken(
                    session, realm, client, user, userSession, clientSessionCtx);

            String accessTokenString = session.tokens().encode(accessToken);
            RefreshToken refreshToken = new RefreshToken(accessToken);
            String refreshTokenString = session.tokens().encode(refreshToken);
            return withCors(Response.ok("{\"access_token\": \"" + accessTokenString + "\", \"refresh_token\": \"" + refreshTokenString + "\"}")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .build());
        } catch (Exception e) {
            logger.error("Token generation failed: " + e.getMessage(), e);
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Token generation failed: " + e.getMessage());
        }
    }

    private String generateChallenge() {
        Challenge challenge = new DefaultChallenge();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.getValue());
    }

    private Response buildErrorResponse(Response.Status status, String message) {
        String safe = message == null ? "" : message.replace("\\", "\\\\").replace("\"", "\\\"");
        return withCors(Response.status(status)
                .entity("{\"error\": \"" + safe + "\"}")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build());
    }

    private byte[] decodeBase64UrlOrStd(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return new byte[0];
        }

        String normalized = base64String.replace('-', '+').replace('_', '/');
        int padding = (4 - (normalized.length() % 4)) % 4;
        normalized = normalized + "=".repeat(padding);

        return Base64.getDecoder().decode(normalized);
    }

    private String normalizeCredentialId(String credentialId) {
        if (credentialId == null || credentialId.isBlank()) {
            return null;
        }
        byte[] credentialBytes = credentialIdToBytes(credentialId);
        if (credentialBytes.length == 0) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(credentialBytes);
    }

    private byte[] credentialIdToBytes(String credentialId) {
        if (credentialId == null || credentialId.isBlank()) {
            return new byte[0];
        }

        try {
            return decodeBase64UrlOrStd(credentialId);
        } catch (IllegalArgumentException ignored) {
            return new byte[0];
        }
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }

    private void trackChallenge(String challenge) {
        clearExpiredChallenges();
        ISSUED_CHALLENGES.put(challenge, System.currentTimeMillis());
    }

    private boolean consumeChallenge(String challenge) {
        if (challenge == null || challenge.isBlank()) {
            return false;
        }

        clearExpiredChallenges();
        Long issuedAt = ISSUED_CHALLENGES.remove(challenge);
        return issuedAt != null && (System.currentTimeMillis() - issuedAt) <= CHALLENGE_TTL_MILLIS;
    }

    private void clearExpiredChallenges() {
        long now = System.currentTimeMillis();
        ISSUED_CHALLENGES.entrySet().removeIf(entry -> (now - entry.getValue()) > CHALLENGE_TTL_MILLIS);
    }

    protected WebAuthnRegistrationManager createWebAuthnRegistrationManager() {
        return WebAuthnRegistrationManager.createNonStrictWebAuthnRegistrationManager(new ObjectConverter());
    }

    private void storeCredentialUserMapping(UserModel user, String credentialId) {
        String normalizedCredentialId = normalizeCredentialId(credentialId);
        if (normalizedCredentialId == null) {
            return;
        }

        List<String> values = new ArrayList<>(user.getAttributeStream(CREDENTIAL_USER_ATTR).toList());
        if (!values.contains(normalizedCredentialId)) {
            values.add(normalizedCredentialId);
            user.setAttribute(CREDENTIAL_USER_ATTR, values);
        }
    }

    private void validateRegistrationCompat(WebAuthnRegistrationManager manager, RegistrationRequest request, RegistrationData data, RegistrationParameters parameters) {
        String[] methodNames = {"verify", "validate"};
        Class<?>[][] signatures = {
                {RegistrationRequest.class, RegistrationParameters.class},
                {RegistrationData.class, RegistrationParameters.class}
        };
        Object[][] args = {
                {request, parameters},
                {data, parameters}
        };

        for (String methodName : methodNames) {
            for (int i = 0; i < signatures.length; i++) {
                try {
                    manager.getClass()
                            .getMethod(methodName, signatures[i][0], signatures[i][1])
                            .invoke(manager, args[i][0], args[i][1]);
                    return;
                } catch (NoSuchMethodException ignored) {
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access WebAuthn registration " + methodName + " method", e);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    }
                    throw new RuntimeException("Passkey registration verification failed", cause);
                }
            }
        }

        throw new RuntimeException("Incompatible WebAuthnRegistrationManager methods: neither verify nor validate is supported");
    }

}
