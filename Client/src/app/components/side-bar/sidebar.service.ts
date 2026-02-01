import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  private visible = signal(false);

  readonly isVisible = this.visible.asReadonly();

  toggle(): void {
    this.visible.update((v) => !v);
  }

  show(): void {
    this.visible.set(true);
  }

  hide(): void {
    this.visible.set(false);
  }
}
