# Setting Up Passkey Authentication in Keycloak

This guide explains how to configure WebAuthn/Passkey authentication in Keycloak for passwordless login.

## Prerequisites

- Keycloak server running and accessible
- Admin access to Keycloak Admin Console
- A realm configured in Keycloak
- Client application configured to use Keycloak for authentication

## Step-by-Step Configuration

### 1. Access Keycloak Admin Console

1. Navigate to the Keycloak Admin Console:
   ```
   http://<keycloak-url>/admin/master/console/
   ```
   Replace `<keycloak-url>` with your Keycloak server URL (e.g., `http://localhost:8081`)

2. Sign in with your admin credentials

### 2. Select Your Realm

1. Click on **"Manage realms"** in the left sidebar
2. Click on the realm you want to configure passkey authentication for
   - For example: `module-management`

### 3. Navigate to Authentication Settings

1. Click on **"Authentication"** in the left sidebar (located at the bottom)
2. This will open the Authentication configuration page

### 4. Configure WebAuthn Passwordless Policy

1. Click on the **"Policies"** tab at the top of the Authentication page
2. Go to the **"WebAuthn Passwordless Policy"** section
3. Locate the **"Enable Passkeys"** switch at the bottom of this section
4. **Turn on** the "Enable Passkeys" switch
5. Optional: Configure other options based on your security requirements
5. Click **"Save"** to apply the changes

> **Note:** Enabling passkeys allows users to register and use passkeys for passwordless authentication. This setting enables the WebAuthn Passwordless flow in Keycloak.

## Client-Side Integration

### Using Keycloak JS

When implementing login in your client application using `keycloak-js`, add the `action` parameter to trigger passkey registration promt:

```typescript
// Example: Angular/TypeScript
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8081',
  realm: 'module-management',
  clientId: 'your-client-id'
});

// Login with passkey registration action
keycloak.login({
  action: 'webauthn-register-passwordless:skip_if_exists'
});
```

## Testing

### Test Passkey Registration

1. Log in to your application
2. If the user doesn't have a passkey, they should be prompted to register one
3. Follow the browser's passkey registration flow
4. Complete the registration using your device's biometric or security key

### Verify Passkey is Registered

1. Log out and log back in
2. You should be able to use your passkey for authentication
3. Or check in Keycloak Admin Console:
   - Go to **Users** → Select a user → **Credentials** tab
   - You should see a WebAuthn credential listed
