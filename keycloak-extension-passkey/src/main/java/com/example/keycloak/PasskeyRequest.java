package com.example.keycloak;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasskeyRequest {

    private String credentialId;

    private String rawId;

    private String attestationObject;

    private String clientDataJSON;

    private String authenticatorData;

    private String signature;

    private String challenge;
}
