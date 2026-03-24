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
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.WebAuthnCredentialModelInput;
import org.keycloak.credential.WebAuthnCredentialProvider;
import org.keycloak.credential.WebAuthnPasswordlessCredentialProviderFactory;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.util.DefaultClientSessionContext;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Path("/")
public class UserPasskeyResource {

    private final KeycloakSession session;
    private static final Logger logger = Logger.getLogger(UserPasskeyResource.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PASSKEY_TYPE = WebAuthnCredentialModel.TYPE_PASSWORDLESS;
    private static final long CHALLENGE_TTL_SECONDS = 120;
    private static final long CHALLENGE_TTL_MILLIS = CHALLENGE_TTL_SECONDS * 1000;
    private static final String CHALLENGE_KEY_PREFIX = "passkey:challenge:";
    private static final String CREDENTIAL_USER_ATTR = "passkey-credential-id";
    private static final String HEADER_ORIGIN = "Origin";
    private final Pattern allowedBrowserOrigin;
    private final String clientId;

    // Needed so CDI can construct this class when bean-discovery-mode="all" is used.
    public UserPasskeyResource() {
        this.session = null;
        this.allowedBrowserOrigin = Pattern.compile("^" + PasskeyConfigResolver.resolveAllowedOriginPatternFromEnv() + "$");
        this.clientId = PasskeyConfigResolver.resolveClientIdFromEnv();
    }

    public UserPasskeyResource(KeycloakSession session, String allowedOriginPattern, String clientId) {
        this.session = session;
        String configuredPattern = PasskeyConfigResolver.firstNonBlank(
                allowedOriginPattern,
                PasskeyConfigResolver.resolveAllowedOriginPatternFromEnv()
        );
        this.allowedBrowserOrigin = Pattern.compile("^" + configuredPattern + "$");
        this.clientId = PasskeyConfigResolver.firstNonBlank(
                clientId,
                PasskeyConfigResolver.resolveClientIdFromEnv()
        );
    }

    private KeycloakSession session() {
        if (session == null) {
            throw new IllegalStateException("UserPasskeyResource must be created by UserPasskeyProvider (SPI-managed session required).");
        }
        return session;
    }

    // ----- CORS -----
    private String getOriginHeader() {
        var headers = session().getContext().getRequestHeaders();
        if (headers == null) {
            return null;
        }
        return headers.getHeaderString(HEADER_ORIGIN);
    }

    private boolean isAllowedOrigin(String origin) {
        return origin != null && allowedBrowserOrigin.matcher(origin.trim()).matches();
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
                .header("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept")
                .header("Access-Control-Max-Age", "3600");
        return withCors(b.build());
    }

    // ----- Endpoints -----
    @GET
    @Path("challenge")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChallenge() {
        return jsonOk(Map.of("challenge", issueChallenge()));
    }

    @GET
    @Path("get-credential-id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCredentialId(@QueryParam("username") String username) {
        String challengeBase64 = issueChallenge();

        if (username == null || username.isBlank()) {
            return jsonOk(Map.of("challenge", challengeBase64));
        }

        UserModel user = findUserByUsername(username);

        if (user == null) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "User not found");
        }

        WebAuthnCredentialModel credentialModel = findFirstPasswordlessCredential(user);
        if (credentialModel == null) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "No passkey found for user");
        }
        String credentialIdBase64 = credentialModel.getWebAuthnCredentialData().getCredentialId();
        return jsonOk(Map.of(
                "credentialId", credentialIdBase64,
                "challenge", challengeBase64
        ));
    }

    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response savePasskey(PasskeyRequest request, @HeaderParam("Authorization") String authorizationHeader) {
        UserModel user = getUserFromBearerToken(authorizationHeader);
        if (user == null) {
            return textResponse(Response.Status.UNAUTHORIZED, "Authenticated user not found from access token");
        }

        try {
            registerPasskey(user, request);
            return textResponse(Response.Status.CREATED, "Passkey stored successfully");
        } catch (IllegalArgumentException | JsonProcessingException e) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "Invalid registration payload: " + e.getMessage());
        }
    }

    // Uses bearer access token subject as the owner of the passkey registration.
    private UserModel getUserFromBearerToken(String authorizationHeader) {
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

        AccessToken accessToken = session().tokens().decode(token, AccessToken.class);
        if (accessToken == null || accessToken.getSubject() == null || accessToken.getSubject().isBlank()) {
            return null;
        }

        RealmModel realm = session().getContext().getRealm();
        return session().users().getUserById(realm, accessToken.getSubject());
    }

    @POST
    @Path("authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticatePasskey(PasskeyRequest request) {
        RealmModel realm = session().getContext().getRealm();

        String requestCredentialId = resolveCredentialId(request);
        if (requestCredentialId == null) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "credentialId or rawId is required");
        }

        if (!consumeChallenge(request.getChallenge())) {
            return buildErrorResponse(Response.Status.UNAUTHORIZED, "Invalid or expired challenge");
        }

        UserModel user = getUserByCredentialId(realm, requestCredentialId);

        if (user == null) {
            return buildErrorResponse(
                    Response.Status.NOT_FOUND,
                    "User not found for credential (username: <unknown>)"
            );
        }

        WebAuthnCredentialModel webAuthnCredential = getWebAuthnCredential(user, requestCredentialId);
        if (webAuthnCredential == null) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "No passkey found for user: " + user.getUsername());
        }

        try {
            boolean isValid = isPasskeyValid(request, requestCredentialId, user, realm);
            if (isValid) {
                return generateTokensResponse(user);
            }
            return buildErrorResponse(Response.Status.UNAUTHORIZED, "Invalid passkey");
        } catch (IllegalArgumentException | JsonProcessingException e) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "Invalid authentication payload: " + e.getMessage());
        }
    }

    // ----- Passkey domain logic -----
    // Registration flow: parse browser payload, verify attestation, and store passkey metadata.
    private void registerPasskey(UserModel user, PasskeyRequest request) throws JsonProcessingException {
        ClientDataContext clientDataContext = parseClientData(request.getClientDataJSON(), null);
        RegistrationRequest registrationRequest = buildRegistrationRequest(request, clientDataContext);
        RegistrationParameters registrationParameters = buildRegistrationParameters(clientDataContext);

        WebAuthnRegistrationManager manager = createWebAuthnRegistrationManager();
        RegistrationData registrationData = manager.parse(registrationRequest);
        validateRegistrationCompat(manager, registrationRequest, registrationData, registrationParameters);

        WebAuthnCredentialModel credentialModel = createCredentialModelFromRegistration(user, registrationData);
        user.credentialManager().createStoredCredential(credentialModel);
        storeCredentialUserMapping(user, credentialModel.getWebAuthnCredentialData().getCredentialId());
    }

    private RegistrationRequest buildRegistrationRequest(PasskeyRequest request, ClientDataContext clientDataContext) {
        byte[] attestationObject = decodeBase64UrlOrStd(request.getAttestationObject());
        return new RegistrationRequest(attestationObject, clientDataContext.decodedClientDataBytes());
    }

    private RegistrationParameters buildRegistrationParameters(ClientDataContext clientDataContext) {
        Set<Origin> originSet = new HashSet<>();
        originSet.add(clientDataContext.origin());
        ServerProperty serverProperty = new ServerProperty(originSet, clientDataContext.rpId(), clientDataContext.challenge(), null);
        return new RegistrationParameters(serverProperty, true);
    }

    private WebAuthnCredentialModel createCredentialModelFromRegistration(UserModel user, RegistrationData registrationData) {
        WebAuthnCredentialModelInput credentialInput = new WebAuthnCredentialModelInput(WebAuthnCredentialModel.TYPE_PASSWORDLESS);
        credentialInput.setAttestedCredentialData(registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData());
        credentialInput.setCount(registrationData.getAttestationObject().getAuthenticatorData().getSignCount());
        credentialInput.setAttestationStatementFormat(registrationData.getAttestationObject().getFormat());
        credentialInput.setTransports(registrationData.getTransports());

        WebAuthnCredentialProvider provider = (WebAuthnCredentialProvider) session()
                .getProvider(CredentialProvider.class, WebAuthnPasswordlessCredentialProviderFactory.PROVIDER_ID);
        WebAuthnCredentialModel rawModel = provider.getCredentialModelFromCredentialInput(credentialInput, user.getUsername());
        return WebAuthnCredentialModel.createFromCredentialModel(rawModel);
    }

    private boolean isPasskeyValid(PasskeyRequest request, String requestCredentialId, UserModel user, RealmModel realm) throws JsonProcessingException {
        byte[] credentialId = decodeBase64UrlOrStd(requestCredentialId);
        byte[] authenticatorData = decodeBase64UrlOrStd(request.getAuthenticatorData());
        byte[] signature = Base64Url.decode(request.getSignature());
        String challenge = request.getChallenge();
        return isPasskeyValid(credentialId, authenticatorData, request.getClientDataJSON(), signature, challenge, user, realm);
    }

    private String resolveCredentialId(PasskeyRequest request) {
        return PasskeyConfigResolver.firstNonBlank(request.getCredentialId(), request.getRawId());
    }

    private String issueChallenge() {
        String challenge = generateChallenge();
        trackChallenge(challenge);
        return challenge;
    }

    private UserModel findUserByUsername(String username) {
        RealmModel realm = session().getContext().getRealm();
        return session().users().getUserByUsername(realm, username);
    }

    private WebAuthnCredentialModel findFirstPasswordlessCredential(UserModel user) {
        return user.credentialManager()
                .getStoredCredentialsStream()
                .filter(cred -> PASSKEY_TYPE.equals(cred.getType()))
                .findFirst()
                .map(WebAuthnCredentialModel::createFromCredentialModel)
                .orElse(null);
    }

    // ----- User/Credential lookup -----
    private UserModel getUserByCredentialId(RealmModel realm, String credentialId) {
        String normalizedCredentialId = normalizeCredentialId(credentialId);
        if (normalizedCredentialId == null) {
            return null;
        }

        UserModel mappedUser = session().users()
                .searchForUserByUserAttributeStream(realm, CREDENTIAL_USER_ATTR, normalizedCredentialId)
                .findFirst()
                .orElse(null);
        if (mappedUser != null) {
            return mappedUser;
        }

        // Fallback scan for environments where user-attribute lookup does not return results reliably.
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
        List<UserModel> users = session().users()
                .searchForUserStream(realm, Map.of("search", ""), first, maxResults)
                .toList();
        if (!users.isEmpty()) {
            return users;
        }

        users = session().users()
                .searchForUserStream(realm, Collections.emptyMap(), first, maxResults)
                .toList();
        if (!users.isEmpty()) {
            return users;
        }

        return session().users()
                .searchForUserStream(realm, "", first, maxResults)
                .toList();
    }

    private WebAuthnCredentialModel getWebAuthnCredential(UserModel user, String credentialId) {
        byte[] requestedCredentialId = credentialIdToBytes(credentialId);
        if (requestedCredentialId.length == 0) {
            return null;
        }

        return user.credentialManager()
                .getStoredCredentialsByTypeStream(PASSKEY_TYPE)
                .map(WebAuthnCredentialModel::createFromCredentialModel)
                .filter(credential -> {
                    String storedCredentialId = credential.getWebAuthnCredentialData().getCredentialId();
                    if (storedCredentialId == null || storedCredentialId.isBlank()) {
                        return false;
                    }

                    byte[] storedCredentialIdBytes = credentialIdToBytes(storedCredentialId);
                    return storedCredentialIdBytes.length > 0 && Arrays.equals(requestedCredentialId, storedCredentialIdBytes);
                })
                .findFirst()
                .orElse(null);
    }

    // Authentication validation: rebuild WebAuthn server properties from signed client payload.
    private boolean isPasskeyValid(byte[] credentialId, byte[] authenticatorData, String clientDataJSON, byte[] signature, String challengeRequest, UserModel user, RealmModel realm) throws JsonProcessingException {
        if (challengeRequest == null || challengeRequest.isBlank()) {
            return false;
        }

        ClientDataContext clientDataContext = parseClientData(clientDataJSON, challengeRequest);
        ServerProperty serverProperty = new ServerProperty(
                clientDataContext.origin(),
                clientDataContext.rpId(),
                clientDataContext.challenge(),
                null
        );

        String uvPolicy = realm.getWebAuthnPolicyPasswordless().getUserVerificationRequirement();
        boolean isUVFlagChecked = uvPolicy != null && "required".equalsIgnoreCase(uvPolicy);
        var authReq = new AuthenticationRequest(
                credentialId,
                authenticatorData,
                clientDataContext.decodedClientDataBytes(),
                signature
        );
        var authParams = new WebAuthnCredentialModelInput.KeycloakWebAuthnAuthenticationParameters(serverProperty, isUVFlagChecked);
        var cred = new WebAuthnCredentialModelInput(PASSKEY_TYPE);

        cred.setAuthenticationRequest(authReq);
        cred.setAuthenticationParameters(authParams);
        return user.credentialManager().isValid(cred);
    }

    // ----- Token issuance -----
    private String buildScopeParameterForClient(ClientModel client) {
        LinkedHashSet<String> scopeNames = new LinkedHashSet<>();
        scopeNames.add(OAuth2Constants.SCOPE_OPENID);
        scopeNames.add(OAuth2Constants.SCOPE_PROFILE);
        scopeNames.add(OAuth2Constants.SCOPE_EMAIL);
        scopeNames.add("roles");
        scopeNames.add("web-origins");
        scopeNames.add("account");
        scopeNames.addAll(client.getClientScopes(true).keySet());
        return String.join(" ", scopeNames);
    }

    private Response generateTokensResponse(UserModel user) {
        try {
            RealmModel realm = session().getContext().getRealm();

            if (clientId == null || clientId.isBlank()) {
                logger.error("Passkey client_id is not configured");
                return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Server configuration error");
            }
            ClientModel client = realm.getClientByClientId(clientId);
            if (client == null) {
                logger.errorf("Client not found for client_id %s", clientId);
                return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Client not found");
            }
            session().getContext().setClient(client);

            ClientConnection connection = session().getContext().getConnection();
            String remoteAddress = connection == null ? "127.0.0.1" : connection.getRemoteAddr();
            UserSessionModel userSession = session().sessions().createUserSession(
                    realm, user, user.getUsername(), remoteAddress, OIDCLoginProtocol.LOGIN_PROTOCOL, false, null, null);
            userSession.setLastSessionRefresh(Time.currentTime());

            AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
            if (clientSession == null) {
                clientSession = session().sessions().createClientSession(realm, client, userSession);
            }
            clientSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
            clientSession.setTimestamp(Time.currentTime());

            String scopeParameter = buildScopeParameterForClient(client);
            clientSession.setNote(OIDCLoginProtocol.SCOPE_PARAM, scopeParameter);
            String issuer = Urls.realmIssuer(session().getContext().getUri().getBaseUri(), realm.getName());
            clientSession.setNote(OIDCLoginProtocol.ISSUER, issuer);
            ClientSessionContext clientSessionCtx = DefaultClientSessionContext.fromClientSessionAndScopeParameter(
                    clientSession, scopeParameter, session());

            if (session().getContext().getClient() == null) {
                logger.error("Client context is still null after setting.");
                return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Client context is null");
            }

            AuthenticationManager.createLoginCookie(
                    session(),
                    realm,
                    user,
                    userSession,
                    session().getContext().getUri(),
                    connection
            );

            EventBuilder event = new EventBuilder(realm, session(), connection)
                    .event(EventType.LOGIN)
                    .client(client)
                    .user(user)
                    .session(userSession);

            TokenManager tokenManager = new TokenManager();
            var tokenBuilder = tokenManager.responseBuilder(
                            realm,
                            client,
                            event,
                            session(),
                            userSession,
                            clientSessionCtx
                    )
                    .generateAccessToken()
                    .generateRefreshToken()
                    .generateIDToken();

            var tokenResponse = tokenBuilder.build();
            event.success();

            return jsonOk(Map.of(
                    "access_token", PasskeyConfigResolver.firstNonBlank(tokenResponse.getToken(), ""),
                    "refresh_token", PasskeyConfigResolver.firstNonBlank(tokenResponse.getRefreshToken(), ""),
                    "id_token", PasskeyConfigResolver.firstNonBlank(tokenResponse.getIdToken(), ""),
                    "expires_in", tokenResponse.getExpiresIn(),
                    "refresh_expires_in", tokenResponse.getRefreshExpiresIn(),
                    "token_type", PasskeyConfigResolver.firstNonBlank(tokenResponse.getTokenType(), "Bearer"),
                    "scope", PasskeyConfigResolver.firstNonBlank(tokenResponse.getScope(), ""),
                    "session_state", PasskeyConfigResolver.firstNonBlank(tokenResponse.getSessionState(), ""),
                    "not-before-policy", tokenResponse.getNotBeforePolicy()
            ));
        } catch (Exception e) {
            logger.error("Token generation failed: " + e.getMessage(), e);
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Token generation failed: " + e.getMessage());
        }
    }

    // ----- Shared payload/codec helpers -----
    private String generateChallenge() {
        Challenge challenge = new DefaultChallenge();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challenge.getValue());
    }

    private Response buildErrorResponse(Response.Status status, String message) {
        return jsonResponse(status, Map.of("error", PasskeyConfigResolver.firstNonBlank(message, "")));
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

    // ----- Challenge storage -----
    private void trackChallenge(String challenge) {
        String challengeKey = challengeKey(challenge);
        Map<String, String> marker = Map.of("issuedAt", Long.toString(System.currentTimeMillis()));
        session().getProvider(SingleUseObjectProvider.class).put(challengeKey, CHALLENGE_TTL_SECONDS, marker);
    }

    private boolean consumeChallenge(String challenge) {
        if (challenge == null || challenge.isBlank()) {
            return false;
        }
        Map<String, String> value = session().getProvider(SingleUseObjectProvider.class).remove(challengeKey(challenge));
        if (value == null) {
            return false;
        }
        try {
            long issuedAt = Long.parseLong(value.getOrDefault("issuedAt", "0"));
            return issuedAt > 0 && (System.currentTimeMillis() - issuedAt) <= CHALLENGE_TTL_MILLIS;
        } catch (NumberFormatException ignored) {
            return false;
        }
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

    // clientDataJSON is the browser-provided WebAuthn metadata payload.
    private ClientDataContext parseClientData(String base64ClientDataJSON, String challengeOverride) throws JsonProcessingException {
        byte[] decodedClientDataBytes = decodeBase64UrlOrStd(base64ClientDataJSON);
        String decodedClientDataJSON = new String(decodedClientDataBytes, StandardCharsets.UTF_8);
        JsonNode clientData = OBJECT_MAPPER.readTree(decodedClientDataJSON);

        String originValue = readRequiredText(clientData, "origin");
        String challengeValue = PasskeyConfigResolver.firstNonBlank(challengeOverride, readRequiredText(clientData, "challenge"));
        return new ClientDataContext(
                decodedClientDataBytes,
                new Origin(originValue),
                extractRpId(originValue),
                new DefaultChallenge(challengeValue)
        );
    }

    private String readRequiredText(JsonNode node, String field) {
        JsonNode valueNode = node.get(field);
        if (valueNode == null || valueNode.asText().isBlank()) {
            throw new IllegalArgumentException("clientDataJSON is missing required field: " + field);
        }
        return valueNode.asText();
    }

    private String extractRpId(String origin) {
        URI parsedUri = URI.create(origin);
        if (parsedUri.getHost() == null || parsedUri.getHost().isBlank()) {
            throw new IllegalArgumentException("Invalid origin in clientDataJSON: " + origin);
        }
        return parsedUri.getHost();
    }

    private Response jsonOk(Object payload) {
        return jsonResponse(Response.Status.OK, payload);
    }

    private Response jsonResponse(Response.Status status, Object payload) {
        String jsonPayload = toJson(payload);
        Response.ResponseBuilder builder = status == Response.Status.OK
                ? Response.ok(jsonPayload)
                : Response.status(status).entity(jsonPayload);
        return withCors(builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).build());
    }

    private Response textResponse(Response.Status status, String payload) {
        return withCors(Response.status(status)
                .entity(payload)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .build());
    }

    private String toJson(Object payload) {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON response", e);
        }
    }

    private record ClientDataContext(byte[] decodedClientDataBytes, Origin origin, String rpId, Challenge challenge) {
    }

    private String challengeKey(String challenge) {
        RealmModel realm = session().getContext().getRealm();
        String realmName = realm == null ? "unknown-realm" : Objects.toString(realm.getName(), "unknown-realm");
        return CHALLENGE_KEY_PREFIX + realmName + ":" + clientId + ":" + challenge;
    }

}
