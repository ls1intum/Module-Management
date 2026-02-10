import { Component, inject, signal, computed } from '@angular/core';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import type { MenuItem } from 'primeng/api';
import { SecurityStore } from '../../core/security/security-store.service';

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterModule, BreadcrumbModule],
  templateUrl: './breadcrumb.component.html'
})
export class BreadcrumbComponent {
  private router = inject(Router);
  private securityStore = inject(SecurityStore);

  private url = signal(this.router.url);

  constructor() {
    this.router.events.pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd)).subscribe((e) => this.url.set(e.url.split('?')[0]));
  }

  home: MenuItem = { icon: 'pi pi-home', routerLink: '/' };

  items = computed<MenuItem[]>(() => this.buildItems(this.url()));

  showBreadcrumb = computed(() => {
    const u = this.url();
    return u.startsWith('/proposals') || u.startsWith('/feedbacks');
  });

  private buildItems(url: string): MenuItem[] {
    if (url.startsWith('/proposals')) return this.buildProposalItems(url);
    if (url.startsWith('/feedbacks')) return this.buildFeedbackItems(url);
    return [];
  }

  private buildProposalItems(url: string): MenuItem[] {
    const segments = url.split('/').filter(Boolean); // ['proposals', ...]
    const items: MenuItem[] = [];

    items.push({ label: 'My Proposals', routerLink: ['/proposals'] });

    if (segments.length <= 1) {
      return items;
    }

    if (segments[1] === 'create') {
      items.push({ label: 'Create Proposal', routerLink: ['/proposals/create'] });
      return items;
    }

    if (segments[1] === 'view' && segments[2]) {
      const proposalId = segments[2];
      items.push({
        label: 'Proposal ' + proposalId,
        routerLink: ['/proposals/view', proposalId]
      });

      if (segments.length <= 3) return items;

      if (segments[3] === 'version' && segments[4]) {
        const versionId = segments[4];
        items.push({
          label: 'Version ' + versionId,
          routerLink: ['/proposals/view', proposalId, 'version', versionId]
        });

        if (segments.length <= 5) return items;

        if (segments[5] === 'edit') {
          items.push({ label: 'Edit', routerLink: ['/proposals/view', proposalId, 'version', versionId, 'edit'] });
          return items;
        }
        if (segments[5] === 'overlap') {
          items.push({
            label: 'Similar Modules',
            routerLink: ['/proposals/view', proposalId, 'version', versionId, 'overlap']
          });
          return items;
        }
      }
    }

    return items;
  }

  private buildFeedbackItems(url: string): MenuItem[] {
    const segments = url.split('/').filter(Boolean); // ['feedbacks', ...]
    const items: MenuItem[] = [];

    items.push({ label: 'Pending Feedbacks', routerLink: ['/feedbacks'] });

    if (segments.length <= 1) {
      return items;
    }

    if (segments[1] === 'view' && segments[2]) {
      items.push({ label: 'Feedback ' + segments[2], routerLink: ['/feedbacks/view', segments[2]] });
    }

    if (segments[3] === 'overlap' && segments[4]) {
      items.push({
        label: 'Similar Modules',
        routerLink: ['/feedbacks/view', segments[2], 'overlap', segments[4]]
      });
    }

    return items;
  }
}
