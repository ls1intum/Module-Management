import { Component, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectModule } from 'primeng/select';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { firstValueFrom } from 'rxjs';
import {
  DegreeProgramSpecializationsControllerService,
  DegreeProgramsControllerService,
  ExaminationBoardControllerService,
  type CreateDegreeProgramSpecializationDTO,
  type DegreeProgramDTO,
  type DegreeProgramSpecializationDTO,
  type ExaminationBoardSummaryDTO
} from '../../../core/modules/openapi';
import { UsersSelectComponent } from '../../../components/users-select/users-select.component';
import { BreadcrumbLabelsService } from '../../../components/breadcrumb/breadcrumb-labels.service';

@Component({
  selector: 'app-degree-program-details-page',
  standalone: true,
  imports: [RouterLink, FormsModule, TableModule, ButtonModule, InputTextModule, DialogModule, MultiSelectModule, SelectModule, TooltipModule, ToastModule, UsersSelectComponent],
  templateUrl: './degree-program-details-page.component.html'
})
export class DegreeProgramDetailsPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly degreeProgramsService = inject(DegreeProgramsControllerService);
  private readonly specializationsService = inject(DegreeProgramSpecializationsControllerService);
  private readonly examinationBoardsService = inject(ExaminationBoardControllerService);
  private readonly messageService = inject(MessageService);
  private readonly breadcrumbLabels = inject(BreadcrumbLabelsService);

  program = signal<DegreeProgramDTO | null>(null);
  allSpecializations = signal<DegreeProgramSpecializationDTO[]>([]);
  loading = signal(true);
  savingProgram = signal(false);
  createDialogVisible = signal(false);
  specializationsToAdd: number[] = [];

  programName = signal('');
  programResponsibleUserId = signal<string | null>(null);

  allExaminationBoards = signal<ExaminationBoardSummaryDTO[]>([]);
  programExaminationBoardId = signal<number | null>(null);
  examinationBoardOptions = computed(() => {
    const boards = this.allExaminationBoards();
    return [{ label: '— No examination board —', value: null as number | null }, ...boards.map((b) => ({ label: b.name ?? '', value: b.examinationBoardId }))];
  });

  newSpecName = signal('');
  newSpecResponsibleUserId = signal<string | null>(null);

  specializationOptions = computed(() => {
    const all = this.allSpecializations();
    const programSpecs = this.program()?.degreeProgramSpecializations ?? [];
    const assignedIds = new Set(programSpecs.map((s) => s.degreeProgramSpecializationId));
    return all.filter((s) => !assignedIds.has(s.degreeProgramSpecializationId)).map((s) => ({ label: s.name, value: s.degreeProgramSpecializationId }));
  });

  constructor() {
    this.route.params.subscribe((params) => {
      const id = params['id'];
      if (id != null) {
        this.loadProgram(Number(id));
      }
    });
  }

  async loadProgram(id: number) {
    this.loading.set(true);
    try {
      const [program, boards] = await Promise.all([
        firstValueFrom(this.degreeProgramsService.getDegreeProgram(id)),
        firstValueFrom(this.examinationBoardsService.getAllExaminationBoards())
      ]);
      this.program.set(program);
      this.allExaminationBoards.set(boards ?? []);
      this.programName.set(program.name ?? '');
      this.programResponsibleUserId.set(program.responsibleUser.userId ?? null);
      this.programExaminationBoardId.set(program.examinationBoard?.examinationBoardId ?? null);
      this.breadcrumbLabels.degreeProgramName.set(program.name ?? null);
      await this.loadAllSpecializations();
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Degree program not found.' });
      this.program.set(null);
      this.breadcrumbLabels.degreeProgramName.set(null);
    } finally {
      this.loading.set(false);
    }
  }

  async saveProgramDetails() {
    const prog = this.program();
    const nameVal = this.programName().trim();
    const userIdVal = this.programResponsibleUserId();
    if (!prog || !nameVal || !userIdVal) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Name and responsible user are required.' });
      return;
    }
    this.savingProgram.set(true);
    try {
      await firstValueFrom(
        this.degreeProgramsService.updateDegreeProgram(prog.degreeProgramId, {
          name: nameVal,
          responsibleUserId: userIdVal,
          examinationBoardId: this.programExaminationBoardId() ?? undefined
        })
      );
      this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'Program details saved.' });
      await this.loadProgram(prog.degreeProgramId);
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to save program details.' });
    } finally {
      this.savingProgram.set(false);
    }
  }

  async loadAllSpecializations() {
    try {
      const list = await firstValueFrom(this.specializationsService.getAllDegreeProgramSpecializations());
      this.allSpecializations.set(list ?? []);
    } catch (e) {
      this.allSpecializations.set([]);
    }
  }

  userLabel(spec: { responsibleUserId?: string | null; responsibleUser?: { firstName?: string; lastName?: string; email?: string } }): string {
    const u = spec?.responsibleUser;
    if (!u) return spec?.responsibleUserId ?? '—';
    return ([u.firstName, u.lastName].filter(Boolean).join(' ').trim() || u.email) ?? spec?.responsibleUserId ?? '—';
  }

  async addSpecializations() {
    const prog = this.program();
    if (!prog || this.specializationsToAdd.length === 0) return;
    try {
      await firstValueFrom(
        this.degreeProgramsService.addSpecializationsToDegreeProgram(prog.degreeProgramId, {
          degreeProgramSpecializationIds: this.specializationsToAdd
        })
      );
      this.messageService.add({
        severity: 'success',
        summary: 'Specializations added',
        detail: `${this.specializationsToAdd.length} specialization(s) assigned.`
      });
      this.specializationsToAdd = [];
      await this.loadProgram(prog.degreeProgramId);
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to add specializations.' });
    }
  }

  async removeSpecialization(degreeProgramSpecializationId: number) {
    const prog = this.program();
    if (!prog) return;
    try {
      await firstValueFrom(this.degreeProgramsService.removeSpecializationFromDegreeProgram(prog.degreeProgramId, degreeProgramSpecializationId));
      this.messageService.add({
        severity: 'success',
        summary: 'Specialization removed',
        detail: 'Specialization unassigned from program.'
      });
      await this.loadProgram(prog.degreeProgramId);
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to remove specialization.' });
    }
  }

  openCreateSpec() {
    this.newSpecName.set('');
    this.newSpecResponsibleUserId.set(null);
    this.createDialogVisible.set(true);
  }

  async createSpecAndAssign() {
    const nameVal = this.newSpecName().trim();
    const userIdVal = this.newSpecResponsibleUserId();
    const prog = this.program();
    if (!nameVal || !userIdVal || !prog) {
      this.messageService.add({ severity: 'warn', summary: 'Validation', detail: 'Name and responsible user are required.' });
      return;
    }
    const dto: CreateDegreeProgramSpecializationDTO = { name: nameVal, responsibleUserId: userIdVal };
    try {
      const newSpec = await firstValueFrom(this.specializationsService.createDegreeProgramSpecialization(dto));
      await firstValueFrom(
        this.degreeProgramsService.addSpecializationsToDegreeProgram(prog.degreeProgramId, {
          degreeProgramSpecializationIds: [newSpec.degreeProgramSpecializationId]
        })
      );
      this.messageService.add({
        severity: 'success',
        summary: 'Specialization created',
        detail: 'New specialization created and assigned to this program.'
      });
      this.createDialogVisible.set(false);
      await this.loadProgram(prog.degreeProgramId);
    } catch (e) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to create specialization.' });
    }
  }
}
