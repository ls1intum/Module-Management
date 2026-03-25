import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { KeycloakService } from './keycloak.service';
import { PasskeyExtensionService } from './passkey-extension.service';
import { firstValueFrom } from 'rxjs';
import { UserControllerService, User } from '../modules/openapi';
import { Passkey } from './keycloak-credentials.types';
import { MessageService } from 'primeng/api';

function passkeyDialogDismissedStorageKey(sub: string): string {
  return `mm_passkey_dialog_dismissed_${sub}`;
}

@Injectable({ providedIn: 'root' })
export class SecurityStore {
  keycloakService = inject(KeycloakService);
  passkeyExtension = inject(PasskeyExtensionService);
  userControllerService = inject(UserControllerService);
  private readonly messageService = inject(MessageService);

  isLoading = signal(false);
  user = signal<User | undefined>(undefined);
  passkeys = signal<Passkey[]>([]);
  passkeyDialogVisible = signal(false);

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
      await this.loadPasskeys();
      try {
        const user = await firstValueFrom(this.userControllerService.getCurrentUser());
        this.user.set(user);
      } catch (error) {
        this.messageService.add({ severity: 'error', summary: 'Sign-in', detail: 'Something went wrong' });
        console.error('error fetching user details', error);
        this.user.set(undefined);
      }
      this.evaluatePasskeyDialogAfterTumLogin();
    }
    this.isLoading.set(false);
  }

  closePasskeyDialog(dontShowAgain: boolean): void {
    if (!this.passkeyDialogVisible()) {
      return;
    }
    if (dontShowAgain) {
      const sub = this.keycloakService.keycloak.tokenParsed?.sub;
      if (sub) {
        localStorage.setItem(passkeyDialogDismissedStorageKey(sub), '1');
      }
    }
    this.passkeyDialogVisible.set(false);
  }

  private evaluatePasskeyDialogAfterTumLogin(): void {
    if (this.passkeys().length > 0) {
      return;
    }
    const sub = this.keycloakService.keycloak.tokenParsed?.sub;
    if (!sub) {
      return;
    }
    if (localStorage.getItem(passkeyDialogDismissedStorageKey(sub)) === '1') {
      return;
    }
    this.passkeyDialogVisible.set(true);
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
      await this.passkeyExtension.signInWithPasskey();
      // Passkey authenticate sets the Keycloak login cookie; reload so init(check-sso)
      // can bootstrap keycloak-js token state through the standard adapter flow.
      window.location.reload();
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
    this.passkeyDialogVisible.set(false);
  }

  async registerPasskey(_returnUrl?: string) {
    await this.passkeyExtension.registerPasskeyInBrowser();
    await this.loadPasskeys();
  }

  async deletePasskey(credentialId: string) {
    try {
      await firstValueFrom(this.keycloakService.deleteCredential(credentialId));
      await this.loadPasskeys();
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
