# Keycloak image with Module-Management passkey realm extension (WebAuthn /passkey/*)
FROM maven:3.9-eclipse-temurin-21 AS extension-build
WORKDIR /build
COPY keycloak-extension-passkey /build/extension
WORKDIR /build/extension
RUN mvn -q -DskipTests package

FROM quay.io/keycloak/keycloak:26.5 AS kc-with-provider
COPY --from=extension-build --chown=keycloak:keycloak \
    /build/extension/target/custom-endpoint-1.0-SNAPSHOT.jar \
    /opt/keycloak/providers/module-management-passkey.jar
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.5
COPY --from=kc-with-provider /opt/keycloak/ /opt/keycloak/
