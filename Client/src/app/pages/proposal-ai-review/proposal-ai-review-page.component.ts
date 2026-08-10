import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AccordionModule } from 'primeng/accordion';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TagModule } from 'primeng/tag';
import {
  ModuleVersionControllerService,
  ProposalAiReviewDTO,
  ProposalAiReviewSectionDTO
} from '../../core/modules/openapi';
import { getProposalReviewSectionLabel } from '../../core/shared/proposal-review-section.utils';

type IssueFilter = 'all' | 'issues';

@Component({
  selector: 'app-proposal-ai-review-page',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    ButtonModule,
    MessageModule,
    TagModule,
    AccordionModule,
    ProgressSpinnerModule,
    SelectButtonModule
  ],
  templateUrl: './proposal-ai-review-page.component.html'
})
export class ProposalAiReviewPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly moduleVersionService = inject(ModuleVersionControllerService);

  review = signal<ProposalAiReviewDTO | null>(null);
  loading = signal(false);
  /** True when regenerating an existing review (vs first load/generation). */
  regenerating = signal(false);
  error = signal<string | null>(null);
  issueFilter = signal<IssueFilter>('issues');
  private moduleVersionId = signal<number | null>(null);

  readonly introText =
    'AI-assisted review of this proposal against the shared review guidelines—highlighting strengths, gaps, and actionable improvements.';

  filterOptions = [
    { label: 'Issues only', value: 'issues' as IssueFilter },
    { label: 'All sections', value: 'all' as IssueFilter }
  ];

  loadingMessage = computed(() =>
    this.regenerating()
      ? 'Regenerating the review… This may take a minute.'
      : 'Analyzing the proposal against review guidelines… This may take a minute.'
  );

  displayedSections = computed(() => {
    const sections = this.review()?.sections ?? [];
    if (this.issueFilter() === 'all') {
      return sections;
    }
    return sections.filter((s) => s.severity !== ProposalAiReviewSectionDTO.SeverityEnum.Ok);
  });

  issueCounts = computed(() => {
    const sections = this.review()?.sections ?? [];
    return {
      critical: sections.filter((s) => s.severity === ProposalAiReviewSectionDTO.SeverityEnum.Critical).length,
      attention: sections.filter((s) => s.severity === ProposalAiReviewSectionDTO.SeverityEnum.Attention).length,
      ok: sections.filter((s) => s.severity === ProposalAiReviewSectionDTO.SeverityEnum.Ok).length
    };
  });

  constructor() {
    this.route.params.subscribe((params) => {
      const moduleVersionId = Number(params['versionId']);
      if (moduleVersionId) {
        this.moduleVersionId.set(moduleVersionId);
        this.loadReview(moduleVersionId);
      }
    });
  }

  /** Loads stored review, or generates one automatically if none exists yet. */
  async loadReview(moduleVersionId: number, regenerate = false) {
    this.loading.set(true);
    this.regenerating.set(regenerate);
    this.error.set(null);
    try {
      const result = await firstValueFrom(
        this.moduleVersionService.getProposalAiReview(moduleVersionId, regenerate)
      );
      this.review.set(result);
    } catch (err) {
      this.error.set(this.errorMessage(err));
    } finally {
      this.loading.set(false);
      this.regenerating.set(false);
    }
  }

  regenerate() {
    const id = this.moduleVersionId() ?? Number(this.route.snapshot.paramMap.get('versionId'));
    if (id) {
      this.loadReview(id, true);
    }
  }

  private errorMessage(err: unknown): string {
    return err instanceof HttpErrorResponse
      ? (typeof err.error === 'string' && err.error ? err.error : err.message)
      : 'Could not generate the AI review. Check that the LLM is configured and try again.';
  }

  sectionLabel(section: ProposalAiReviewSectionDTO): string {
    return section.sectionLabel ?? getProposalReviewSectionLabel(section.section as never) ?? section.section ?? 'Section';
  }

  severityLabel(severity: ProposalAiReviewSectionDTO.SeverityEnum | undefined): string {
    switch (severity) {
      case ProposalAiReviewSectionDTO.SeverityEnum.Critical:
        return 'Critical';
      case ProposalAiReviewSectionDTO.SeverityEnum.Attention:
        return 'Attention';
      case ProposalAiReviewSectionDTO.SeverityEnum.Ok:
        return 'OK';
      default:
        return 'Unknown';
    }
  }

  severityTag(severity: ProposalAiReviewSectionDTO.SeverityEnum | undefined): 'danger' | 'warn' | 'success' | 'secondary' {
    switch (severity) {
      case ProposalAiReviewSectionDTO.SeverityEnum.Critical:
        return 'danger';
      case ProposalAiReviewSectionDTO.SeverityEnum.Attention:
        return 'warn';
      case ProposalAiReviewSectionDTO.SeverityEnum.Ok:
        return 'success';
      default:
        return 'secondary';
    }
  }
}
