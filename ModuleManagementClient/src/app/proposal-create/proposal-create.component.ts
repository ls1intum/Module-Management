import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProposalControllerService } from '../core/modules/openapi';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-proposal-create',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './proposal-create.component.html',
  styleUrl: './proposal-create.component.css'
})
export class ProposalsCreateComponent implements OnInit {
  proposalForm: FormGroup;
  users = [
    { id: 1, name: 'Professor1' },
    { id: 2, name: 'Professor2' }
  ];

  constructor(private formBuilder: FormBuilder, private proposalService: ProposalControllerService) {
    this.proposalForm = this.formBuilder.group({
      userId: [null, Validators.required],
      titleEng: ['', Validators.required],
      levelEng: ['', Validators.required],
      languageEng: ['', Validators.required],
      repetitionEng: ['', Validators.required],
      frequencyEng: ['', Validators.required],
      credits: [null, [Validators.required, Validators.min(0)]],
      hoursTotal: [null, [Validators.required, Validators.min(0)]],
      hoursSelfStudy: [null, [Validators.required, Validators.min(0)]],
      hoursPresence: [null, [Validators.required, Validators.min(0)]],
      examinationAchievementsEng: ['', Validators.required],
      recommendedPrerequisitesEng: ['', Validators.required],
      contentEng: [''],
      learningOutcomesEng: ['', Validators.required],
      teachingMethodsEng: ['', Validators.required],
      mediaEng: ['', Validators.required],
      literatureEng: ['', Validators.required],
      responsiblesEng: ['', Validators.required],
      lvSwsLecturerEng: ['', Validators.required]
    });
  }

  ngOnInit(): void {}

  async onSubmit() {
    if (this.proposalForm.valid) {
      const proposalData = this.proposalForm.value;
      try {
        console.log("HAHAHAHAH")
        await this.proposalService.createProposal(proposalData);
        // todo: hanle positve
      } catch (error) {
        console.log("ERROR")
        console.error('Error creating proposal:', error);
      }
    }
  }
}
