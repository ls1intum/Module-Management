package com.example.keycloak;

import org.keycloak.Config;

final class PasskeyConfigResolver {

    private static final String DEFAULT_ALLOWED_ORIGIN = "http://localhost:3000";
    private static final String DEFAULT_CLIENT_ID = "demo-app";
    private static final String ENV_ALLOWED_BROWSER_ORIGIN = "KC_ALLOWED_BROWSER_ORIGIN";
    private static final String ENV_DEMO_ADDITIONAL_WEB_ORIGIN = "KC_DEMO_ADDITIONAL_WEB_ORIGIN";
    private static final String ENV_PASSKEY_CLIENT_ID = "KC_PASSKEY_CLIENT_ID";

    private PasskeyConfigResolver() {
    }

    static String resolveAllowedOriginPattern(Config.Scope config) {
        return firstNonBlank(
                config == null ? null : config.get("allowed-browser-origin"),
                System.getenv(ENV_ALLOWED_BROWSER_ORIGIN),
                System.getenv(ENV_DEMO_ADDITIONAL_WEB_ORIGIN),
                DEFAULT_ALLOWED_ORIGIN
        );
    }

    static String resolveClientId(Config.Scope config) {
        return firstNonBlank(
                config == null ? null : config.get("client-id"),
                System.getenv(ENV_PASSKEY_CLIENT_ID),
                DEFAULT_CLIENT_ID
        );
    }

    static String resolveAllowedOriginPatternFromEnv() {
        return firstNonBlank(
                System.getenv(ENV_ALLOWED_BROWSER_ORIGIN),
                System.getenv(ENV_DEMO_ADDITIONAL_WEB_ORIGIN),
                DEFAULT_ALLOWED_ORIGIN
        );
    }

    static String resolveClientIdFromEnv() {
        return firstNonBlank(
                System.getenv(ENV_PASSKEY_CLIENT_ID),
                DEFAULT_CLIENT_ID
        );
    }

    static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
