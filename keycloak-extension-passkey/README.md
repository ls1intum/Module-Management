# Keycloak Passkey Extension Integration (Quick Guide)

## Admin/Dev quick summary

To use this extension, you install one custom Keycloak provider JAR and configure Keycloak plus realm/client settings to match your environment. Required customizations are:

- Deploy `custom-endpoint-1.0-SNAPSHOT.jar` to `keycloak/providers/` and restart Keycloak.
- Set `KC_PASSKEY_CLIENT_ID` to the OIDC client used for browser login completion.
- Set `KC_ALLOWED_BROWSER_ORIGIN` to your client origin(s) for CORS.
- Enable Passwordless WebAuthn in the realm and set the Passwordless RP ID to your app host.
- Configure the matching client with correct `Web Origins` and `Redirect URIs`.
- Configure silent `check-sso` in your SPA and serve a callback page (for example `/silent-check-sso.html`).

After that, your client calls `/realms/{realm}/passkey/{challenge|save|authenticate}` with `fetch(..., { credentials: 'include' })`. The extension sets CORS via Keycloak's `Cors.auth()` behavior (including `Access-Control-Allow-Credentials: true`) for cross-origin credentialed requests.

---

## How to use it?

This extension adds passkey APIs to Keycloak at:

`/realms/{realm}/passkey/*`

Endpoints:

- `GET /challenge`
- `POST /save`
- `POST /authenticate`

## How the plugin works

The plugin is a Keycloak `RealmResourceProvider` mounted at `/realms/{realm}/passkey/*`.

1. `GET /challenge` creates a short-lived, single-use challenge in Keycloak server storage.
2. `POST /save` stores a verified passkey for the currently logged-in user (resolved from the bearer access token).
3. `POST /authenticate` verifies the WebAuthn assertion, completes the standard Keycloak browser login flow (including required actions), sets the Keycloak login cookie, and returns `204 No Content`.

How `check-sso` uses that session:

- The client calls `/authenticate` with `credentials: 'include'` so the Keycloak login cookie is written in the browser.
- After successful authentication, run `keycloak.init({ onLoad: 'check-sso' })` again to hydrate tokens from the new cookie-backed browser session (silent mode recommended).
- `check-sso` uses the existing Keycloak browser session (cookie) to silently authenticate and provide fresh tokens in `keycloak.token`/`keycloak.tokenParsed` for subsequent API calls.

## 1. Build and deploy

```bash
cd keycloak-custom-passkey-login
mvn clean package
cp target/custom-endpoint-1.0-SNAPSHOT.jar ../keycloak/providers/
```

Restart Keycloak after copying the JAR.

## 2. Configure Keycloak env vars

- `KC_PASSKEY_CLIENT_ID`: OIDC client used by `/authenticate` to complete browser login flow.
- `KC_ALLOWED_BROWSER_ORIGIN`: CORS origin regex.

Example:

```env
KC_PASSKEY_CLIENT_ID=my-spa-client
KC_ALLOWED_BROWSER_ORIGIN=https?://(localhost|127\.0\.0\.1|\[::1\])(:\d+)?
```

## 3. Configure realm/client

In your realm:

1. Enable Passwordless WebAuthn.
2. Set Passwordless RP ID to your app host (e.g. `localhost` in local dev).
3. Ensure client `clientId == KC_PASSKEY_CLIENT_ID`.
4. Add your client URL to `Web Origins`.
5. Add callback URLs to `Redirect URIs`.
6. Add your silent check-sso callback URL to `Redirect URIs` (for example `http://localhost:3000/silent-check-sso.html`, or covered by wildcard).

## 4. One client module (`keycloakClient.js`)

### 4.1 Add silent check-sso callback page

Create `public/silent-check-sso.html`:

```html
<!doctype html>
<html lang="en">
  <body>
    <script>parent.postMessage(location.href, location.origin);</script>
  </body>
</html>
```

### 4.2 Create one `keycloakClient.js`

```js
import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: 'http://localhost:8080',
  realm: 'demo',
  clientId: 'demo-app'
};

const checkSsoOptions = {
  onLoad: 'check-sso',
  pkceMethod: 'S256',
  silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
  silentCheckSsoFallback: false
};

let keycloak = new Keycloak(keycloakConfig);

function toBase64Url(buffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function fromBase64Url(value) {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
  return Uint8Array.from(atob(padded), (c) => c.charCodeAt(0));
}

function passkeyUrl(path) {
  return `${keycloakConfig.url}/realms/${encodeURIComponent(keycloakConfig.realm)}/passkey/${path}`;
}

async function getChallenge() {
  const res = await fetch(passkeyUrl('challenge'), { credentials: 'include' });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(body.error || 'Failed to get challenge');
  return body.challenge;
}

export async function initAuth() {
  keycloak = new Keycloak(keycloakConfig);
  return keycloak.init(checkSsoOptions);
}

export function loginWithPassword() {
  return keycloak.login({ redirectUri: window.location.href });
}

export async function registerPasskey() {
  if (!keycloak.authenticated || !keycloak.token) {
    throw new Error('User must be logged in first');
  }

  const challenge = await getChallenge();
  const username = keycloak.tokenParsed?.preferred_username || keycloak.tokenParsed?.sub || 'user';
  const displayName = keycloak.tokenParsed?.name || username;
  const userIdBytes = new TextEncoder().encode(username).slice(0, 64);

  const credential = await navigator.credentials.create({
    publicKey: {
      challenge: fromBase64Url(challenge),
      rp: { name: 'My App', id: window.location.hostname },
      user: { id: userIdBytes, name: username, displayName },
      pubKeyCredParams: [{ type: 'public-key', alg: -7 }],
      authenticatorSelection: { residentKey: 'required', userVerification: 'preferred' },
      attestation: 'none'
    }
  });

  const res = await fetch(passkeyUrl('save'), {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${keycloak.token}`
    },
    body: JSON.stringify({
      credentialId: toBase64Url(credential.rawId),
      rawId: toBase64Url(credential.rawId),
      clientDataJSON: toBase64Url(credential.response.clientDataJSON),
      attestationObject: toBase64Url(credential.response.attestationObject),
      challenge
    })
  });

  if (!res.ok) throw new Error(await res.text());
}

export async function loginWithPasskey() {
  const challenge = await getChallenge();
  const assertion = await navigator.credentials.get({
    publicKey: { challenge: fromBase64Url(challenge), userVerification: 'preferred' }
  });

  const res = await fetch(passkeyUrl('authenticate'), {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({
      credentialId: toBase64Url(assertion.rawId),
      rawId: toBase64Url(assertion.rawId),
      clientDataJSON: toBase64Url(assertion.response.clientDataJSON),
      authenticatorData: toBase64Url(assertion.response.authenticatorData),
      signature: toBase64Url(assertion.response.signature),
      challenge
    })
  });

  if (res.status !== 204) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `Passkey auth failed: ${res.status}`);
  }

  const authenticated = await initAuth(); // silent check-sso refresh
  if (!authenticated) throw new Error('No session after passkey auth');
}

export function logout() {
  return keycloak.logout({ redirectUri: `${window.location.origin}/` });
}

export function getKeycloak() {
  return keycloak;
}
```

### 4.3 Use these functions from anywhere in your client

```js
import { initAuth, login, loginWithPasskey, registerPasskey, logout, getKeycloak } from './keycloakClient.js';

// call once on app startup
const authenticated = await initAuth();

// later, for example on button clicks
await loginWithPassword();

await loginWithPasskey();

await registerPasskey();

await logout();
```

## 5. Important notes

- Challenge TTL is `120s` and single-use.
- `/challenge` and `/save` should be called with `credentials: 'include'` so auth cookies/sessions are preserved.
- `/save` requires `Authorization: Bearer <token>` and the issued registration `challenge`.
- `/authenticate` should be called with `credentials: 'include'`; on success it typically returns `204` after writing login cookies.
- Re-run `check-sso` after `/authenticate` to hydrate tokens into your SPA client (prefer `silentCheckSsoRedirectUri` + `silentCheckSsoFallback: false`).
- If you get CORS errors, fix `KC_ALLOWED_BROWSER_ORIGIN` and client `Web Origins`.
