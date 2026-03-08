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
      'bulletPoints',
      'credits',
      'frequencyEng',
      'hoursLecture',
      'hoursExercise',
      'hoursPractical',
      'hoursSeminar',
      'firstSemesterAvailable',
      'successorModuleName',
      'levelEng',
      'languageEng'
    ],
    requiredControlNames: ['titleEng']
  },
  {
    id: 'schedule-workload',
    title: 'Schedule & workload',
    controlNames: ['duration', 'repetitionEng', 'hoursTotal', 'hoursSelfStudy', 'hoursPresence', 'credits']
  },
  {
    id: 'examination-prereqs',
    title: 'Examination & prerequisites',
    controlNames: ['examinationAchievementsEng', 'examinationAchievementsPromptEng', 'recommendedPrerequisitesEng']
  },
  {
    id: 'content-learning-teaching',
    title: 'Content, learning & teaching',
    controlNames: [
      'contentEng',
      'contentPromptEng',
      'learningOutcomesEng',
      'learningOutcomesPromptEng',
      'teachingMethodsEng',
      'teachingMethodsPromptEng'
    ]
  },
  {
    id: 'media-literature',
    title: 'Media, literature & responsibles',
    controlNames: ['mediaEng', 'literatureEng', 'responsiblesEng', 'lvSwsLecturerEng']
  }
];
