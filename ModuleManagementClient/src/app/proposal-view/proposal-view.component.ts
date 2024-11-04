import { Component, OnInit } from '@angular/core';
import { ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO } from '../core/modules/openapi';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-proposal-view',
  templateUrl: './proposal-view.component.html',
  styleUrls: ['./proposal-view.component.css']
})
export class ProposalViewComponent implements OnInit {
  proposal: ProposalViewDTO | null = null;
  loading: boolean = false;
  error: string | null = null;
  proposalStatusEnum = Proposal.StatusEnum;
  moduleStatusEnum = ModuleVersion.StatusEnum;

  constructor(
    private proposalService: ProposalControllerService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const proposalId = Number(this.route.snapshot.paramMap.get('id'));
    if (proposalId) {
      this.loading = true;
      this.proposalService.getProposalView(+proposalId).subscribe({
        next: (data: ProposalViewDTO) => this.proposal = data,
        error: (err) => this.error = 'Failed to load proposal data' ,
        complete: () => this.loading = false
      });
    }
  }

  viewModuleVersion(versionId: number | undefined) {
    this.router.navigate(['/module-version/view', versionId]);
  }

  editModuleVersion(versionId: number | undefined) {
    this.router.navigate(['/module-version/edit', versionId]);
  }

  editLatestModuleVersion() {
    if (this.proposal) {
      this.router.navigate(['/module-version/edit', this.proposal.latestVersion]);
    }
  }

  addNewModuleVersion() {
    if (this.proposal) {
      this.router.navigate(['/module-version/add'], { queryParams: { proposalId: this.proposal.proposalId } });
    }
  }
}
