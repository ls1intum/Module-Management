import { Component, OnInit } from '@angular/core';
import { ModuleVersion, Proposal, ProposalControllerService, ProposalViewDTO } from '../core/modules/openapi';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-proposal-view',
  standalone: true,
  templateUrl: './proposal-view.component.html',
  imports: [RouterModule],
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
        error: (err) => this.error = err,
        complete: () => this.loading = false
      });
    }
  }

  viewModuleVersion(versionId: number | undefined) {
    this.router.navigate(['/module-version/view', versionId]);
  }

  editLatestModuleVersion() {
    if (this.proposal) {
      console.log(this.proposal)
      this.router.navigate(['/module-version/edit', this.proposal.latestVersion]);
    }
  }

  addNewModuleVersion() {
    if (this.proposal) {
      this.router.navigate(['/module-version/add'], { queryParams: { proposalId: this.proposal.proposalId } });
    }
  }
}
