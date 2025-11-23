import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { KeycloakService } from './keycloak.service';
import { firstValueFrom } from 'rxjs';
import { UserControllerService, User } from '../modules/openapi';
import { Passkey } from './keycloak-credentials.types';

@Injectable({ providedIn: 'root' })
export class SecurityStore {
  keycloakService = inject(KeycloakService);
  userControllerService = inject(UserControllerService);

  isLoading = signal(false);
  user = signal<User | undefined>(undefined);
  passkeys = signal<Passkey[]>([]);

  constructor() {
    this.onInit();
  }

  async onInit() {
    const isServer = isPlatformServer(inject(PLATFORM_ID));
    if (isServer) {
      this.user.set(undefined);
      return;
    }
    this.isLoading.set(true);

    const isLoggedIn = await this.keycloakService.init();

    if (isLoggedIn) {
      this.loadPasskeys();
      try {
        const user = await firstValueFrom(this.userControllerService.getCurrentUser());
        this.user.set(user);
      } catch (error) {
        console.error('error fetching user details', error);
        this.user.set(undefined);
      }
    }
    this.isLoading.set(false);
  }

  async signIn(returnUrl?: string) {
    await this.keycloakService.login(returnUrl);
  }

  async signOut() {
    await this.keycloakService.logout();
    this.user.set(undefined);
    this.passkeys.set([]);
  }

  async registerPasskey(returnUrl?: string) {
    return await this.keycloakService.registerPasskey(returnUrl);
  }

  async deletePasskey(credentialId: string) {
    try {
      await firstValueFrom(this.keycloakService.deleteCredential(credentialId));
      this.loadPasskeys();
    } catch (error) {
      console.error('Error deleting passkey:', error);
    }
  }

  async loadPasskeys() {
    try {
      const keycloakCredentials = await firstValueFrom(this.keycloakService.getCredentials());
      const passkeys =
        keycloakCredentials
          .find((credential) => credential.type === 'webauthn-passwordless')
          ?.userCredentialMetadatas.map((metadata) => {
            return {
              id: metadata.credential.id,
              name: metadata.credential.userLabel,
              createdAt: metadata.credential.createdDate ? new Date(metadata.credential.createdDate) : undefined
            };
          }) ?? [];
      this.passkeys.set(passkeys);
    } catch (error) {
      console.error('Error reloading passkeys:', error);
      this.passkeys.set([]);
    }
  }
}
