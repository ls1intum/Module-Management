import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { isPlatformServer } from '@angular/common';
import { KeycloakService } from './keycloak.service';
import { ANONYMOUS_USER, User } from './models';

@Injectable({ providedIn: 'root' })
export class SecurityStore {
  keycloakService = inject(KeycloakService);

  loaded = signal(false);
  user = signal<User | undefined>(undefined);

  loadedUser = computed(() => (this.loaded() ? this.user() : undefined));
  signedIn = computed(() => this.loaded() && !this.user()?.anonymous);

  constructor() {
    this.onInit();
  }

  async onInit() {
    const isServer = isPlatformServer(inject(PLATFORM_ID));
    const keycloakService = inject(KeycloakService);
    if (isServer) {
      this.user.set(ANONYMOUS_USER);
      this.loaded.set(true);
      return;
    }

    const isLoggedIn = await keycloakService.init();
    if (isLoggedIn && keycloakService.profile) {
      const { sub, email, token, roles, name, preferred_username: username } = keycloakService.profile;
      const user = {
        id: sub,
        email,
        name,
        username,
        anonymous: false,
        bearer: token,
        roles
      };
      this.user.set(user);
      this.loaded.set(true);
    } else {
      this.user.set(ANONYMOUS_USER);
      this.loaded.set(true);
    }
  }

  async signIn(returnUrl?: string) {
    await this.keycloakService.login(returnUrl);
  }

  async signOut() {
    await this.keycloakService.logout();
  }

  async updateToken() {
    await this.keycloakService.updateToken();
    const user = this.user();
    if (user && this.keycloakService.profile) {
      user.bearer = this.keycloakService.profile.token;
      this.user.set(user);
    }
  }
}
