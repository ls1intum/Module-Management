/**
 * Module version / proposal fields that can be linked from a pre-submission guideline.
 * Keep keys in sync with {@code ModuleVersionGuidelineFieldKeys} on the server.
 */
export const MODULE_VERSION_GUIDELINE_RELATED_FIELDS: { value: string; label: string }[] = [
  { value: 'bulletPoints', label: 'Key points' },
  { value: 'titleEng', label: 'Title' },
  { value: 'titleDe', label: 'Title (German)' },
  { value: 'levelEng', label: 'Level' },
  { value: 'languageEng', label: 'Language' },
  { value: 'frequencyEng', label: 'Frequency' },
  { value: 'credits', label: 'Credits' },
  { value: 'hoursLecture', label: 'Hours (lecture)' },
  { value: 'hoursExercise', label: 'Hours (exercise)' },
  { value: 'hoursPractical', label: 'Hours (practical)' },
  { value: 'hoursSeminar', label: 'Hours (seminar)' },
  { value: 'firstSemesterAvailable', label: 'First semester available' },
  { value: 'successorModuleName', label: 'Successor module' },
  { value: 'duration', label: 'Duration' },
  { value: 'hoursTotal', label: 'Total hours' },
  { value: 'hoursSelfStudy', label: 'Self-study hours' },
  { value: 'hoursPresence', label: 'Presence hours' },
  { value: 'examinationAchievementsEng', label: 'Examination achievements' },
  { value: 'repetitionEng', label: 'Repetition' },
  { value: 'recommendedPrerequisitesEng', label: 'Recommended prerequisites' },
  { value: 'contentEng', label: 'Module content' },
  { value: 'learningOutcomesEng', label: 'Learning outcomes' },
  { value: 'teachingMethodsEng', label: 'Teaching methods' },
  { value: 'mediaEng', label: 'Media' },
  { value: 'literatureEng', label: 'Literature' },
  { value: 'responsiblesEng', label: 'Responsibles' },
  { value: 'lvSwsLecturerEng', label: 'Lecturer (SWS)' }
];

export function labelForModuleVersionFieldKey(key: string | null | undefined): string {
  if (!key) return '—';
  return MODULE_VERSION_GUIDELINE_RELATED_FIELDS.find((o) => o.value === key)?.label ?? key;
}
