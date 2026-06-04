import { AiReviewGuidelineDTO } from '../modules/openapi';

export const PROPOSAL_REVIEW_SECTION_OPTIONS: { label: string; value: AiReviewGuidelineDTO.SectionEnum }[] = [
  { label: 'General (whole proposal)', value: AiReviewGuidelineDTO.SectionEnum.General },
  { label: 'Title (English)', value: AiReviewGuidelineDTO.SectionEnum.TitleEng },
  { label: 'Title (German)', value: AiReviewGuidelineDTO.SectionEnum.TitleDe },
  { label: 'Level', value: AiReviewGuidelineDTO.SectionEnum.LevelEng },
  { label: 'Language', value: AiReviewGuidelineDTO.SectionEnum.LanguageEng },
  { label: 'Frequency', value: AiReviewGuidelineDTO.SectionEnum.FrequencyEng },
  { label: 'Credits', value: AiReviewGuidelineDTO.SectionEnum.Credits },
  { label: 'Duration', value: AiReviewGuidelineDTO.SectionEnum.Duration },
  { label: 'Hours (Lecture)', value: AiReviewGuidelineDTO.SectionEnum.HoursLecture },
  { label: 'Hours (Exercise)', value: AiReviewGuidelineDTO.SectionEnum.HoursExercise },
  { label: 'Hours (Practical)', value: AiReviewGuidelineDTO.SectionEnum.HoursPractical },
  { label: 'Hours (Seminar)', value: AiReviewGuidelineDTO.SectionEnum.HoursSeminar },
  { label: 'First semester available', value: AiReviewGuidelineDTO.SectionEnum.FirstSemesterAvailable },
  { label: 'Successor module', value: AiReviewGuidelineDTO.SectionEnum.SuccessorModuleName },
  { label: 'Total hours', value: AiReviewGuidelineDTO.SectionEnum.HoursTotal },
  { label: 'Self-study hours', value: AiReviewGuidelineDTO.SectionEnum.HoursSelfStudy },
  { label: 'Presence hours', value: AiReviewGuidelineDTO.SectionEnum.HoursPresence },
  { label: 'Key points', value: AiReviewGuidelineDTO.SectionEnum.BulletPoints },
  { label: 'Examination achievements', value: AiReviewGuidelineDTO.SectionEnum.ExaminationAchievements },
  { label: 'Repetition', value: AiReviewGuidelineDTO.SectionEnum.Repetition },
  { label: 'Recommended prerequisites', value: AiReviewGuidelineDTO.SectionEnum.RecommendedPrerequisites },
  { label: 'Module content', value: AiReviewGuidelineDTO.SectionEnum.Content },
  { label: 'Learning outcomes', value: AiReviewGuidelineDTO.SectionEnum.LearningOutcomes },
  { label: 'Teaching methods', value: AiReviewGuidelineDTO.SectionEnum.TeachingMethods },
  { label: 'Media', value: AiReviewGuidelineDTO.SectionEnum.Media },
  { label: 'Literature', value: AiReviewGuidelineDTO.SectionEnum.Literature },
  { label: 'Responsibles', value: AiReviewGuidelineDTO.SectionEnum.Responsibles },
  { label: 'Lecturer', value: AiReviewGuidelineDTO.SectionEnum.LvSwsLecturer },
  { label: 'Degree program assignments', value: AiReviewGuidelineDTO.SectionEnum.DegreeProgramAssignments }
];

export function getProposalReviewSectionLabel(section: AiReviewGuidelineDTO.SectionEnum | undefined): string {
  if (!section) return '';
  return PROPOSAL_REVIEW_SECTION_OPTIONS.find((o) => o.value === section)?.label ?? section;
}
