import { inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { KeycloakService } from './keycloak.service';
import { firstValueFrom } from 'rxjs';
import { UserControllerService, User } from '../modules/openapi';

@Injectable({ providedIn: 'root' })
export class SecurityStore {
  keycloakService = inject(KeycloakService);
  userControllerService = inject(UserControllerService);

  isLoading = signal(false);
  user = signal<User | undefined>(undefined);

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
  }
}
