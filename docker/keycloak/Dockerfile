# Keycloak image with Module-Management passkey realm extension (WebAuthn /passkey/*)
FROM maven:3.9-eclipse-temurin-21 AS extension-build
WORKDIR /build/extension
COPY keycloak-extension-passkey/ /build/extension/
# Resolve built JAR to a fixed path (artifactId/version may change; mvn failure fails the stage).
RUN --mount=type=cache,target=/root/.m2 \
    mvn -ntp -DskipTests package \
    && cp target/custom-endpoint-*.jar /passkey-provider.jar

FROM quay.io/keycloak/keycloak:26.5 AS kc-with-provider
COPY --from=extension-build --chown=keycloak:keycloak \
    /passkey-provider.jar \
    /opt/keycloak/providers/module-management-passkey.jar
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.5
COPY --from=kc-with-provider /opt/keycloak/ /opt/keycloak/
