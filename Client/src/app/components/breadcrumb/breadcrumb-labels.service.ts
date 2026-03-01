import { Injectable, signal } from '@angular/core';

/** Segment-specific breadcrumb labels. Pages set the relevant signal when they load data and set to null on destroy. */
@Injectable({ providedIn: 'root' })
export class BreadcrumbLabelsService {
  /** Degree program details page: program name. */
  readonly degreeProgramName = signal<string | null>(null);
  /** Proposal/view segment: module title (e.g. from latestModuleVersion.titleEng). */
  readonly proposalTitle = signal<string | null>(null);
  /** Version segment: e.g. "Version 2" from moduleVersion.version. */
  readonly versionLabel = signal<string | null>(null);
  /** Feedback view segment: e.g. module title from the feedback's module version. */
  readonly feedbackLabel = signal<string | null>(null);
}
