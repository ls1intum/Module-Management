package com.example.keycloak;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

import java.util.Map;
import java.util.regex.Pattern;

@Path("/")
public class UserPasskeyResource {

    private static final Logger logger = Logger.getLogger(UserPasskeyResource.class);
    private static final String ERROR_SERVER_CONFIGURATION = "Server configuration error";
    private static final String ERROR_INVALID_OR_EXPIRED_CHALLENGE = "Invalid or expired challenge";
    private static final String ERROR_REQUEST_BODY_REQUIRED = "Request body is required";

    private final KeycloakSession session;
    private final Pattern allowedBrowserOrigin;
    private final String clientId;
    private final PasskeyClientSupport clientSupport;

    /**
     * Default constructor for CDI environments that require a no-arg constructor.
     * <p>
     * The SPI-managed constructor should be used for normal runtime operation.
     */
    public UserPasskeyResource() {
        this.session = null;
        this.allowedBrowserOrigin = Pattern.compile("^" + PasskeyConfigResolver.resolveAllowedOriginPatternFromEnv() + "$");
        this.clientId = PasskeyConfigResolver.resolveClientIdFromEnv();
        this.clientSupport = new PasskeyClientSupport(this.clientId);
    }

    /**
     * Creates the passkey resource with SPI-provided session and resolved configuration.
     *
     * @param session Keycloak request session
     * @param allowedOriginPattern configured allowed browser origin regex (without anchors)
     * @param clientId configured OIDC client identifier used for passkey flows
     */
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
        this.clientSupport = new PasskeyClientSupport(this.clientId);
    }

    /**
     * Handles browser CORS preflight requests for all passkey endpoints.
     *
     * @return preflight response with CORS headers when client configuration is available
     */
    @OPTIONS
    @Path("{any:.*}")
    public Response corsPreflight() {
        Response.ResponseBuilder responseBuilder = Response.ok();
        applyCors(responseBuilder, true);
        return responseBuilder.build();
    }

    /**
     * Issues a short-lived challenge used by registration and authentication requests.
     *
     * @return JSON object containing a base64url challenge value
     */
    @GET
    @Path("challenge")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChallenge() {
        try {
            return jsonOk(Map.of("challenge", challengeService().issueChallenge()));
        } catch (IllegalStateException e) {
            return handleServerConfigurationError("Passkey challenge creation failed due to server configuration", e);
        }
    }

    /**
     * Stores a new passwordless WebAuthn credential for the authenticated bearer token user.
     *
     * @param request passkey registration payload
     * @return created response on success, error response otherwise
     */
    @POST
    @Path("save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response savePasskey(PasskeyRequest request) {
        if (request == null) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, ERROR_REQUEST_BODY_REQUIRED);
        }

        UserModel user = getUserFromBearerToken();
        if (user == null) {
            return textResponse(Response.Status.UNAUTHORIZED, "Authenticated user not found from access token");
        }

        if (!challengeService().consumeChallenge(request.getChallenge())) {
            return buildErrorResponse(Response.Status.UNAUTHORIZED, ERROR_INVALID_OR_EXPIRED_CHALLENGE);
        }

        try {
            webAuthnService().registerPasskey(user, request, request.getChallenge());
            return textResponse(Response.Status.CREATED, "Passkey stored successfully");
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "Invalid registration payload: " + e.getMessage());
        } catch (IllegalStateException e) {
            return handleServerConfigurationError("Passkey registration failed due to server configuration", e);
        }
    }

    /**
     * Verifies a passkey assertion and continues the regular Keycloak browser login flow.
     *
     * @param request passkey authentication payload
     * @return browser flow response on success, error response otherwise
     */
    @POST
    @Path("authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticatePasskey(PasskeyRequest request) {
        if (request == null) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, ERROR_REQUEST_BODY_REQUIRED);
        }

        RealmModel realm = session().getContext().getRealm();
        if (realm == null) {
            return handleServerConfigurationError("Realm context unavailable for passkey authentication", new IllegalStateException("Realm context is unavailable"));
        }

        String requestCredentialId = webAuthnService().resolveCredentialId(request);
        if (requestCredentialId == null) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "credentialId or rawId is required");
        }

        if (!challengeService().consumeChallenge(request.getChallenge())) {
            return buildErrorResponse(Response.Status.UNAUTHORIZED, ERROR_INVALID_OR_EXPIRED_CHALLENGE);
        }

        UserModel user = webAuthnService().findUserByCredentialId(realm, requestCredentialId);
        if (user == null) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "User not found for credential");
        }

        if (!webAuthnService().hasPasskeyCredential(user, requestCredentialId)) {
            return buildErrorResponse(Response.Status.NOT_FOUND, "No passkey found for user: " + user.getUsername());
        }

        try {
            if (!webAuthnService().authenticatePasskey(user, request, requestCredentialId)) {
                return buildErrorResponse(Response.Status.UNAUTHORIZED, "Invalid passkey");
            }

            return withCors(browserLoginService().completeLogin(user, realm));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(Response.Status.BAD_REQUEST, "Invalid authentication payload: " + e.getMessage());
        } catch (IllegalStateException e) {
            return handleServerConfigurationError("Passkey authentication failed due to server configuration", e);
        } catch (Exception e) {
            logger.error("Browser-flow completion after passkey authentication failed: " + e.getMessage(), e);
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Authentication flow failed");
        }
    }

    /**
     * Resolves the current user from the bearer token in request headers using Keycloak's authenticator.
     *
     * @return authenticated user, or {@code null} when token validation fails
     */
    private UserModel getUserFromBearerToken() {
        RealmModel realm = session().getContext().getRealm();
        if (realm == null) {
            return null;
        }

        AuthenticationManager.AuthResult authResult;
        try {
            authResult = new AppAuthManager.BearerTokenAuthenticator(session())
                    .setRealm(realm)
                    .setConnection(session().getContext().getConnection())
                    .setUriInfo(session().getContext().getUri())
                    .setHeaders(session().getContext().getRequestHeaders())
                    .authenticate();
        } catch (NotAuthorizedException ignored) {
            return null;
        } catch (RuntimeException ignored) {
            return null;
        }

        if (authResult == null || authResult.getUser() == null) {
            return null;
        }

        if (clientId != null && !clientId.isBlank()) {
            ClientModel tokenClient = authResult.getClient();
            if (tokenClient == null || !clientId.equals(tokenClient.getClientId())) {
                return null;
            }
        }

        return authResult.getUser();
    }

    /**
     * Returns the active SPI session and fails fast if the resource was created without it.
     */
    private KeycloakSession session() {
        if (session == null) {
            throw new IllegalStateException("UserPasskeyResource must be created by UserPasskeyProvider (SPI-managed session required).");
        }
        return session;
    }

    /**
     * Creates the challenge service for the current request context.
     */
    private PasskeyChallengeService challengeService() {
        return new PasskeyChallengeService(session(), clientSupport);
    }

    /**
     * Creates the WebAuthn service for the current request context.
     */
    private PasskeyWebAuthnService webAuthnService() {
        return new PasskeyWebAuthnService(session(), allowedBrowserOrigin);
    }

    /**
     * Creates the browser-login service for the current request context.
     */
    private PasskeyBrowserLoginService browserLoginService() {
        return new PasskeyBrowserLoginService(session(), clientSupport);
    }

    /**
     * Logs and returns a uniform internal-server-error response for configuration problems.
     */
    private Response handleServerConfigurationError(String logMessage, IllegalStateException exception) {
        logger.error(logMessage, exception);
        return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ERROR_SERVER_CONFIGURATION);
    }

    /**
     * Builds a 200 JSON response wrapped with CORS headers.
     */
    private Response jsonOk(Object payload) {
        return jsonResponse(Response.Status.OK, payload);
    }

    /**
     * Builds a JSON response with CORS headers.
     */
    private Response jsonResponse(Response.Status status, Object payload) {
        Response.ResponseBuilder builder = status == Response.Status.OK
                ? Response.ok(payload)
                : Response.status(status).entity(payload);
        return withCors(builder.type(MediaType.APPLICATION_JSON_TYPE).build());
    }

    /**
     * Builds a plain-text response with CORS headers.
     */
    private Response textResponse(Response.Status status, String payload) {
        return withCors(Response.status(status)
                .entity(payload)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build());
    }

    /**
     * Builds a Keycloak-style error response and applies CORS headers.
     */
    private Response buildErrorResponse(Response.Status status, String message) {
        ErrorResponseException errorResponse = ErrorResponse.error(
                PasskeyConfigResolver.firstNonBlank(message, ""),
                status
        );
        return withCors(errorResponse.getResponse());
    }

    /**
     * Rebuilds the response while appending CORS headers for configured clients.
     */
    private Response withCors(Response response) {
        Response.ResponseBuilder responseBuilder = Response.fromResponse(response);
        applyCors(responseBuilder, false);
        return responseBuilder.build();
    }

    /**
     * Applies Keycloak CORS settings based on configured client web origins.
     *
     * @param responseBuilder response builder to mutate
     * @param preflight whether to apply preflight-specific headers
     */
    private void applyCors(Response.ResponseBuilder responseBuilder, boolean preflight) {
        ClientModel corsClient = clientSupport.resolveConfiguredClient(session().getContext().getRealm());
        if (corsClient == null) {
            return;
        }

        Cors cors = Cors.builder()
                .builder(responseBuilder)
                .auth()
                .allowedMethods("GET", "POST", "OPTIONS");

        if (preflight) {
            cors.preflight();
        }

        cors.allowedOrigins(session(), corsClient);
        cors.add();
    }
}
