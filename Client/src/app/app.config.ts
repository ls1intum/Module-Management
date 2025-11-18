import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { securityInterceptor } from './core/security/security-interceptor';
import { BASE_PATH } from './core/modules/openapi/variables';
import { environment } from '../../environments/environment';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([securityInterceptor])),
    ReactiveFormsModule,
    {
      provide: BASE_PATH,
      useValue: environment.serverUrl
    }
  ]
};
