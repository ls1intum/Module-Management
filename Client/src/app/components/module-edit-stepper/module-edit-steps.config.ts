import { Action } from 'rxjs/internal/scheduler/Action';

/** Status of a step in the stepper; determines how the step is displayed (number, check, or pending color). */
export const StepperStatus = {
  Default: 'default',
  Pending: 'pending',
  Completed: 'completed',
  ActionRequired: 'action-required'
} as const;

export type StepperStatus = (typeof StepperStatus)[keyof typeof StepperStatus];

export interface ModuleEditStepConfig {
  id: string;
  title: string;
  controlNames: string[];
  requiredControlNames?: string[];
}

export const MODULE_EDIT_STEPS: ModuleEditStepConfig[] = [
  {
    id: 'basic',
    title: 'Basic information & assignment',
    controlNames: [
      'titleEng',
      'titleDe',
      // 'bulletPoints',
      'credits',
      'frequencyEng',
      'hoursLecture',
      'hoursExercise',
      'hoursPractical',
      'hoursSeminar',
      'firstSemesterAvailable',
      'successorModuleName',
      // 'levelEng',
      'languageEng'
    ],
    requiredControlNames: ['titleEng']
  },
  {
    id: 'submit-coordinator-feedback',
    title: 'Submit for coordinator feedback',
    controlNames: []
  },
  {
    id: 'schedule-workload',
    title: 'Schedule & workload',
    controlNames: ['duration', 'repetitionEng', 'hoursTotal', 'hoursSelfStudy', 'hoursPresence', 'credits']
  },
  {
    id: 'examination-prereqs',
    title: 'Examination & prerequisites',
    controlNames: ['examinationAchievementsEng', 'recommendedPrerequisitesEng']
  },
  {
    id: 'content-learning-teaching',
    title: 'Content, learning & teaching',
    controlNames: ['contentEng', 'learningOutcomesEng', 'teachingMethodsEng']
  },
  {
    id: 'media-literature',
    title: 'Media, literature & responsibles',
    controlNames: ['mediaEng', 'literatureEng', 'responsiblesEng', 'lvSwsLecturerEng']
  },
  {
    id: 'submit-full-feedback',
    title: 'Submit for full feedback',
    controlNames: []
  }
];
