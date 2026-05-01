import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { KeycloakService } from './keycloak.service';

/**
 * In-app WebAuthn via Keycloak realm extension (same flow as ba-test-keycloak {@code public/app.js}):
 * register: {@code /passkey/challenge} + {@code /passkey/save}; sign-in: {@code /passkey/get-credential-id} + {@code /passkey/authenticate}.
 */
@Injectable({ providedIn: 'root' })
export class PasskeyExtensionService {
  private readonly keycloakService = inject(KeycloakService);

  private passkeyBaseUrl(): string {
    const base = environment.keycloak.url.replace(/\/$/, '');
    const realm = encodeURIComponent(environment.keycloak.realm);
    return `${base}/realms/${realm}/passkey`;
  }

  private getUrl(path: string): string {
    const p = path.replace(/^\/+/, '');
    return `${this.passkeyBaseUrl()}/${p}`;
  }

  private base64UrlToUint8Array(value: string): Uint8Array {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    return Uint8Array.from(atob(padded), (c) => c.charCodeAt(0));
  }

  private bufferToBase64Url(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.length; i += 1) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }

  private async readJsonBody<T>(response: Response): Promise<T | undefined> {
    const contentType = response.headers.get('content-type') ?? '';
    if (!contentType.toLowerCase().includes('application/json')) {
      return undefined;
    }
    try {
      return (await response.json()) as T;
    } catch {
      return undefined;
    }
  }

  /**
   * Register a new passkey for the current user (must already be logged in).
   */
  async registerPasskeyInBrowser(): Promise<void> {
    const kc = this.keycloakService.keycloak;
    if (!kc.authenticated || !kc.token) {
      throw new Error('You must be signed in to register a passkey.');
    }

    await kc.updateToken(60);
    const token = kc.token;
    if (!token) {
      throw new Error('No access token available.');
    }

    const parsed = kc.tokenParsed as Record<string, unknown> | undefined;
    const accountId = String(parsed?.['sub'] ?? parsed?.['preferred_username'] ?? '');
    const accountName = String(parsed?.['preferred_username'] ?? parsed?.['email'] ?? '');
    const displayName = String(parsed?.['name'] ?? ([parsed?.['given_name'], parsed?.['family_name']].filter(Boolean).join(' ') || accountName || 'User'));

    if (!accountId || !accountName) {
      throw new Error('Missing user identity in token for passkey registration.');
    }

    const challengeRes = await fetch(this.getUrl('challenge'), { credentials: 'include' });
    if (!challengeRes.ok) {
      throw new Error(`Failed to get WebAuthn challenge (${challengeRes.status})`);
    }
    const { challenge } = (await challengeRes.json()) as { challenge: string };
    if (!challenge) {
      throw new Error('Invalid challenge response from Keycloak');
    }

    const userIdBytes = new TextEncoder().encode(accountId).slice(0, 64);

    const credential = (await navigator.credentials.create({
      publicKey: {
        challenge: this.base64UrlToUint8Array(challenge) as BufferSource,
        rp: { name: 'Module Management', id: window.location.hostname },
        user: { id: userIdBytes, name: accountName, displayName },
        pubKeyCredParams: [{ type: 'public-key', alg: -7 }],
        authenticatorSelection: { userVerification: 'preferred', residentKey: 'required' },
        attestation: 'none'
      }
    })) as PublicKeyCredential | null;

    if (!credential?.response) {
      throw new Error('Passkey creation was cancelled or failed.');
    }

    const response = credential.response as AuthenticatorAttestationResponse;
    const savePayload = {
      credentialId: this.bufferToBase64Url(credential.rawId),
      rawId: this.bufferToBase64Url(credential.rawId),
      clientDataJSON: this.bufferToBase64Url(response.clientDataJSON),
      attestationObject: this.bufferToBase64Url(response.attestationObject),
      challenge
    };

    const saveRes = await fetch(this.getUrl('save'), {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(savePayload)
    });

    const saveText = await saveRes.text();
    if (!saveRes.ok) {
      throw new Error(saveText || `Failed to store passkey (${saveRes.status})`);
    }

    await kc.updateToken(-1);
  }

  /**
   * Sign in with passkey only (no Keycloak UI redirect).
   * The extension endpoint sets the Keycloak login cookie; the SPA should then reload
   * and let keycloak-js initialize via check-sso.
   */
  async signInWithPasskey(): Promise<void> {
    const optionsResponse = await fetch(this.getUrl('challenge'), { credentials: 'include' });
    const res = await this.readJsonBody<{ challenge?: string; credentialId?: string; error?: string }>(optionsResponse);
    if (!optionsResponse.ok) {
      throw new Error(res?.error || `Failed to get passkey options (${optionsResponse.status})`);
    }
    if (!res?.challenge) {
      throw new Error('Invalid challenge response from server');
    }

    const publicKey: PublicKeyCredentialRequestOptions = {
      challenge: this.base64UrlToUint8Array(res.challenge) as BufferSource,
      userVerification: 'preferred'
    };
    if (res.credentialId) {
      publicKey.allowCredentials = [{ type: 'public-key', id: this.base64UrlToUint8Array(res.credentialId) as BufferSource }];
    }

    const credential = (await navigator.credentials.get({ publicKey })) as PublicKeyCredential | null;
    if (!credential?.response) {
      throw new Error('Passkey sign-in was cancelled or failed.');
    }

    const ar = credential.response as AuthenticatorAssertionResponse;
    const payload = {
      credentialId: this.bufferToBase64Url(credential.rawId),
      rawId: this.bufferToBase64Url(credential.rawId),
      clientDataJSON: this.bufferToBase64Url(ar.clientDataJSON),
      authenticatorData: this.bufferToBase64Url(ar.authenticatorData),
      signature: this.bufferToBase64Url(ar.signature),
      challenge: res.challenge
    };

    const authRes = await fetch(this.getUrl('authenticate'), {
      method: 'POST',
      credentials: 'include',
      redirect: 'manual',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (authRes.type === 'opaqueredirect') {
      return;
    }

    const authResult = await this.readJsonBody<{ error?: string }>(authRes);
    if (!authRes.ok) {
      throw new Error(authResult?.error || `Passkey authentication failed (${authRes.status})`);
    }
  }
}
