import { Component, inject, signal, computed } from '@angular/core';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import type { MenuItem } from 'primeng/api';
import { BreadcrumbLabelsService } from './breadcrumb-labels.service';

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterModule, BreadcrumbModule],
  templateUrl: './breadcrumb.component.html'
})
export class BreadcrumbComponent {
  private router = inject(Router);
  private breadcrumbLabels = inject(BreadcrumbLabelsService);

  private url = signal(this.router.url);

  constructor() {
    this.router.events.pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd)).subscribe((e) => this.url.set(e.url.split('?')[0]));
  }

  home: MenuItem = { icon: 'pi pi-home', routerLink: '/' };

  items = computed<MenuItem[]>(() => this.buildItems(this.url()));

  showBreadcrumb = computed(() => {
    const u = this.url();
    return u.startsWith('/proposals') || u.startsWith('/feedbacks') || u.startsWith('/admin');
  });

  private buildItems(url: string): MenuItem[] {
    if (url.startsWith('/proposals')) return this.buildProposalItems(url);
    if (url.startsWith('/feedbacks')) return this.buildFeedbackItems(url);
    if (url.startsWith('/admin')) return this.buildAdminItems(url);
    return [];
  }

  private buildAdminItems(url: string): MenuItem[] {
    const segments = url.split('/').filter(Boolean); // ['admin', 'degree-programs', ...]
    const items: MenuItem[] = [];

    if (segments.length < 2) return items;

    if (segments[1] === 'users') {
      items.push({ label: 'Users', routerLink: ['/admin/users'] });
      return items;
    }

    if (segments[1] === 'examination-boards') {
      items.push({ label: 'Examination boards', routerLink: ['/admin/examination-boards'] });
      if (segments.length > 2 && segments[2] && segments[2] !== 'specializations') {
        const boardId = segments[2];
        const label =
          (this.breadcrumbLabels.examinationBoardName() ?? '').trim() || `Board ${boardId}`;
        items.push({
          label,
          routerLink: ['/admin/examination-boards', boardId]
        });
      }
      return items;
    }

    if (segments[1] === 'degree-programs') {
      items.push({ label: 'Degree Programs', routerLink: ['/admin/degree-programs'] });
      if (segments.length <= 2) return items;

      if (segments[2] === 'specializations') {
        items.push({ label: 'All areas of specializations', routerLink: ['/admin/degree-programs/specializations'] });
        return items;
      }

      // degree-programs/:id (program details page) – label from details page when it loads the program
      const programId = segments[2];
      const name = (this.breadcrumbLabels.degreeProgramName() ?? '').trim() || `Program ${programId}`;
      items.push({
        label: name,
        routerLink: ['/admin/degree-programs', programId]
      });
      return items;
    }

    return items;
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

    // segments[1] is proposal id (e.g. /proposals/123 or /proposals/123/version/456/edit)
    const proposalId = segments[1];
    if (!proposalId) return items;

    const proposalLabel = (this.breadcrumbLabels.proposalTitle() ?? '').trim() || `Proposal ${proposalId}`;
    items.push({
      label: proposalLabel,
      routerLink: ['/proposals', proposalId]
    });

    if (segments.length <= 2) return items;

    if (segments[2] === 'version' && segments[3]) {
      const versionId = segments[3];
      const versionSegmentLabel = (this.breadcrumbLabels.versionLabel() ?? '').trim() || `Version ${versionId}`;
      items.push({
        label: versionSegmentLabel,
        routerLink: ['/proposals', proposalId, 'version', versionId]
      });

      if (segments.length <= 4) return items;

      if (segments[4] === 'edit') {
        items.push({ label: 'Edit', routerLink: ['/proposals', proposalId, 'version', versionId, 'edit'] });
        return items;
      }
      if (segments[4] === 'overlap') {
        items.push({
          label: 'Similar Modules',
          routerLink: ['/proposals', proposalId, 'version', versionId, 'overlap']
        });
        return items;
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

    if (segments[1] === 'pre-submission-guidelines') {
      items.push({
        label: 'Pre-submission guidelines',
        routerLink: ['/feedbacks/pre-submission-guidelines']
      });
      return items;
    }

    if (segments[1] === 'view' && segments[2]) {
      const label = (this.breadcrumbLabels.feedbackLabel() ?? '').trim() || `Feedback ${segments[2]}`;
      items.push({ label, routerLink: ['/feedbacks/view', segments[2]] });
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
