import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments/environment';
import Keycloak from 'keycloak-js';
import {KeycloakCredentialType} from './keycloak-credentials.types';

function toBase64Url(buffer: ArrayBuffer) {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function fromBase64Url(value: string) {
  const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
  return Uint8Array.from(atob(padded), (c) => c.charCodeAt(0));
}

@Injectable({providedIn: 'root'})
export class KeycloakService {
  checkSsoOptions = {
    onLoad: 'check-sso' as const,
    pkceMethod: 'S256' as const,
    silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
    silentCheckSsoFallback: false
  };
  private readonly http = inject(HttpClient);

  private _keycloak?: Keycloak;

  get keycloak(): Keycloak {
    if (!this._keycloak) {
      throw new Error('Keycloak not initialized');
    }
    return this._keycloak;
  }

  passkeyUrl(path: string) {
    return `${environment.keycloak.url}/realms/${encodeURIComponent(environment.keycloak.realm)}/passkey/${path}`;
  }

  async getChallenge() {
    const res = await fetch(this.passkeyUrl('challenge'), {credentials: 'include'});
    const body = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(body.error || 'Failed to get challenge');
    return body.challenge;
  }

  initAuth(): Promise<boolean> {
    this._keycloak = new Keycloak(environment.keycloak);
    return this._keycloak.init(this.checkSsoOptions);
  }

  updateToken(minValidity = 60): Promise<boolean> {
    if (!this.keycloak.authenticated) {
      return Promise.resolve(false);
    }
    return this.keycloak.updateToken(minValidity);
  }

  signInWithTum(returnUrl?: string) {
    return this.keycloak.login({redirectUri: returnUrl || window.location.href});
  }

  async registerPasskey() {
    if (!this.keycloak.authenticated || !this.keycloak.token) {
      throw new Error('User must be logged in first');
    }

    const challenge = await this.getChallenge();
    const username = (this.keycloak.tokenParsed?.['preferred_username'] as string | undefined) || this.keycloak.tokenParsed?.sub || 'user';
    const displayName = (this.keycloak.tokenParsed?.['name'] as string | undefined) || username;
    const userIdBytes = new TextEncoder().encode(username).slice(0, 64);

    const credential = await navigator.credentials.create({
      publicKey: {
        challenge: fromBase64Url(challenge),
        rp: {name: 'Module Management', id: window.location.hostname},
        user: {id: userIdBytes, name: username, displayName},
        pubKeyCredParams: [{type: 'public-key', alg: -7}],
        authenticatorSelection: {residentKey: 'required', userVerification: 'preferred'},
        attestation: 'none'
      }
    });
    if (!(credential instanceof PublicKeyCredential)) {
      throw new Error('Failed to create public key credential');
    }
    if (!(credential.response instanceof AuthenticatorAttestationResponse)) {
      throw new Error('Invalid attestation response');
    }

    const res = await fetch(this.passkeyUrl('save'), {
      method: 'POST', credentials: 'include', headers: {
        'Content-Type': 'application/json', Authorization: `Bearer ${this.keycloak.token}`
      }, body: JSON.stringify({
        credentialId: toBase64Url(credential.rawId),
        rawId: toBase64Url(credential.rawId),
        clientDataJSON: toBase64Url(credential.response.clientDataJSON),
        attestationObject: toBase64Url(credential.response.attestationObject),
        challenge
      })
    });

    if (!res.ok) throw new Error(await res.text());
  }

  async signInWithPasskey() {
    const challenge = await this.getChallenge();
    const assertion = await navigator.credentials.get({
      publicKey: {challenge: fromBase64Url(challenge), userVerification: 'preferred'}
    });
    if (!(assertion instanceof PublicKeyCredential)) {
      throw new Error('Failed to create public key assertion');
    }
    if (!(assertion.response instanceof AuthenticatorAssertionResponse)) {
      throw new Error('Invalid assertion response');
    }

    const res = await fetch(this.passkeyUrl('authenticate'), {
      method: 'POST',
      credentials: 'include',
      headers: {'Content-Type': 'application/json', Accept: 'application/json'},
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

    const authenticated = await this.initAuth(); // silent check-sso refresh
    if (!authenticated) throw new Error('No session after passkey auth');
  }

  getCredentials() {
    const url = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/account/credentials`;
    return this.http.get<KeycloakCredentialType[]>(url);
  }

  deleteCredential(credentialId: string) {
    const url = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/account/credentials/${credentialId}`;
    return this.http.delete<unknown>(url);
  }

  logout() {
    return this.keycloak.logout({redirectUri: environment.redirect});
  }
}
