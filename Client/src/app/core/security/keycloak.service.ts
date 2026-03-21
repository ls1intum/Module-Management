import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import Keycloak, { type KeycloakTokenParsed } from 'keycloak-js';
import { KeycloakCredentialType } from './keycloak-credentials.types';

@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private http = inject(HttpClient);
  _keycloak: Keycloak | undefined;

  get keycloak() {
    if (!this._keycloak) {
      this._keycloak = new Keycloak({
        url: environment.keycloak.url,
        realm: environment.keycloak.realm,
        clientId: environment.keycloak.clientId
      });
    }
    return this._keycloak;
  }

  async init() {
    return await this.keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      silentCheckSsoFallback: true,
      checkLoginIframe: true,
      pkceMethod: 'S256'
    });
  }

  get bearer() {
    return this.keycloak.token;
  }

  /**
   * Update access token if it is about to expire or has expired
   * This is independent from the silent check sso or refresh token validity.
   * @returns
   */
  async updateToken() {
    if (!this.keycloak.isTokenExpired(60)) {
      return false;
    }
    try {
      // Try to refresh token
      return await this.keycloak.updateToken(60);
    } catch (error) {
      console.error('Failed to refresh token:', error);
      // Redirect to login if refresh fails
      await this.keycloak.login();
      return false;
    }
  }

  loginWithTumRedirect(returnUrl?: string) {
    return this.keycloak.login({ redirectUri: window.location.origin + (returnUrl ?? '') });
  }

  applyPasskeyTokens(accessToken: string, refreshToken: string): void {
    const kc = this.keycloak;
    kc.token = accessToken;
    kc.refreshToken = refreshToken;
    kc.idToken = undefined;
    kc.idTokenParsed = undefined;

    const parsed = this.parseJwtPayload(accessToken);
    kc.tokenParsed = parsed;
    kc.refreshTokenParsed = this.parseJwtPayload(refreshToken);

    if (parsed) {
      kc.subject = parsed.sub;
      const ext = parsed as KeycloakTokenParsed & { sid?: string; session_state?: string };
      kc.sessionId = ext.sid ?? ext.session_state;
      kc.realmAccess = parsed.realm_access;
      kc.resourceAccess = parsed.resource_access;
    }
    kc.timeSkew = 0;
    kc.authenticated = true;
  }

  private parseJwtPayload(token: string): KeycloakTokenParsed | undefined {
    try {
      const parts = token.split('.');
      if (parts.length < 2) {
        return undefined;
      }
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
      return JSON.parse(atob(padded)) as KeycloakTokenParsed;
    } catch {
      return undefined;
    }
  }

  logout() {
    return this.keycloak.logout({ redirectUri: environment.redirect });
  }

  getCredentials() {
    const url = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/account/credentials`;
    return this.http.get<KeycloakCredentialType[]>(url);
  }

  deleteCredential(credentialId: string) {
    const url = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/account/credentials/${credentialId}`;
    return this.http.delete<any[]>(url);
  }
}
