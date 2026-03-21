import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { KeycloakService } from './keycloak.service';
import { PasskeyExtensionService } from './passkey-extension.service';
import { firstValueFrom } from 'rxjs';
import { UserControllerService, User } from '../modules/openapi';
import { Passkey } from './keycloak-credentials.types';
import { MessageService } from 'primeng/api';

@Injectable({ providedIn: 'root' })
export class SecurityStore {
  keycloakService = inject(KeycloakService);
  passkeyExtension = inject(PasskeyExtensionService);
  userControllerService = inject(UserControllerService);
  private readonly messageService = inject(MessageService);

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
        const user = await firstValueFrom(
          this.userControllerService.getCurrentUser('body', false, { transferCache: false })
        );
        this.user.set(user);
      } catch (error) {
        this.messageService.add({ severity: 'error', summary: 'Sign-in', detail: 'Something went wrong' });
        console.error('error fetching user details', error);
        this.user.set(undefined);
      }
    }
    this.isLoading.set(false);
  }

  async signInWithTum(returnUrl?: string) {
    await this.keycloakService.loginWithTumRedirect(returnUrl);
  }

  async signIn(returnUrl?: string) {
    await this.signInWithTum(returnUrl);
  }

  async signInWithPasskey(): Promise<void> {
    this.isLoading.set(true);
    try {
      const tokens = await this.passkeyExtension.signInWithPasskey();
      this.keycloakService.applyPasskeyTokens(tokens.access_token, tokens.refresh_token);
      await this.loadPasskeys();
      const user = await firstValueFrom(
        this.userControllerService.getCurrentUser('body', false, { transferCache: false })
      );
      this.user.set(user);
    } catch (error) {
      this.messageService.add({ severity: 'error', summary: 'Sign-in', detail: 'Something went wrong' });
      console.error(error);
    } finally {
      this.isLoading.set(false);
    }
  }

  async signOut() {
    await this.keycloakService.logout();
    this.user.set(undefined);
    this.passkeys.set([]);
  }

  async registerPasskey(_returnUrl?: string) {
    await this.passkeyExtension.registerPasskeyInBrowser();
    await this.loadPasskeys();
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
