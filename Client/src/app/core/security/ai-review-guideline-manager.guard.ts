import { inject, Injectable, Injector } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { filter, map, Observable, switchMap, take } from 'rxjs';
import { isAiReviewGuidelineManagerRole } from '../shared/user-role.utils';
import { SecurityStore } from './security-store.service';

@Injectable({ providedIn: 'root' })
export class AiReviewGuidelineManagerGuard implements CanActivate {
  private readonly injector = inject(Injector);
  private readonly securityStore = inject(SecurityStore);
  private readonly router = inject(Router);

  canActivate(): Observable<boolean | UrlTree> {
    return toObservable(this.securityStore.isLoading, { injector: this.injector }).pipe(
      filter((loading) => !loading),
      take(1),
      switchMap(() => toObservable(this.securityStore.user, { injector: this.injector }).pipe(take(1))),
      map((user) => {
        if (isAiReviewGuidelineManagerRole(user?.roles)) {
          return true;
        }
        return this.router.createUrlTree(['/']);
      })
    );
  }
}
