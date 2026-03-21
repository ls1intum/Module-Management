import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { KeycloakService } from './keycloak.service';

/**
 * In-app WebAuthn passkey registration via Keycloak realm extension
 * {@code /realms/{realm}/passkey/save} (no redirect to Keycloak login UI).
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
    const displayName = String(
      parsed?.['name'] ??
        ([parsed?.['given_name'], parsed?.['family_name']].filter(Boolean).join(' ') || accountName || 'User')
    );

    if (!accountId || !accountName) {
      throw new Error('Missing user identity in token for passkey registration.');
    }

    const challengeRes = await fetch(this.getUrl('challenge'));
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
      attestationObject: this.bufferToBase64Url(response.attestationObject)
    };

    const saveRes = await fetch(this.getUrl('save'), {
      method: 'POST',
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
}
