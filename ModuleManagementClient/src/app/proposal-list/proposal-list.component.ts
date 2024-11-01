import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ProposalsCompactDTO } from '../core/modules/openapi';
import { lastValueFrom } from 'rxjs';

@Component({
  selector: 'app-proposal-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './proposal-list.component.html',
  styleUrl: './proposal-list.component.css'
})
export class ProposalListComponent {
  proposalService = inject(HttpClient);
  proposals: ProposalsCompactDTO[] = [];
  users = [
    { id: 1, name: 'Professor1' },
    { id: 2, name: 'Professor2' },
    { id: 3, name: 'QM User' },
    { id: 4, name: 'ASA User' },
    { id: 5, name: 'EB User' },
    { id: 6, name: 'Admin'}
  ];
  loading: boolean = true;
  error: string | null = null;

  constructor() {
    this.fetchProposals();
  }

  async onUserChange(event: Event) {
    const selectElement = event.target as HTMLSelectElement;
    const userId = parseInt(selectElement.value, 10);

    this.loading = true;
    const selectedUser = this.users.find(user => user.id === userId);
    const isProfessor = selectedUser?.name.includes('Professor');
    const endpoint = isProfessor ? `http://localhost:8080/api/proposals/compact/from-user/${userId}` : 'http://localhost:8080/api/proposals/compact';

    await this.fetchProposals(endpoint);
  }

  private async fetchProposals(endpoint = 'http://localhost:8080/api/proposals/compact') {
    try {
      const proposals = await lastValueFrom(this.proposalService.get<ProposalsCompactDTO[]>(endpoint));
      this.proposals = proposals;
    } catch (err) {
      this.error = 'Failed to load proposals';
      console.error(err);
    } finally {
      this.loading = false;
    }
  }
}