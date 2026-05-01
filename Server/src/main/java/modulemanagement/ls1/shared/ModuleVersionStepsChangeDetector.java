package modulemanagement.ls1.shared;

import modulemanagement.ls1.dtos.ModuleDegreeProgramAssignmentDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.models.ModuleVersion;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ModuleVersionStepsChangeDetector {

    private ModuleVersionStepsChangeDetector() {
    }

    public static boolean isStep1DataChanged(ModuleVersionUpdateRequestDTO request, ModuleVersion mv) {
        // Step-1 (basic + assignment) fields.
        if (!Objects.equals(normalize(request.getTitleEng()), normalize(mv.getTitleEng())))
            return true;
        if (!Objects.equals(normalize(request.getTitleDe()), normalize(mv.getTitleDe())))
            return true;
        if (!Objects.equals(request.getCredits(), mv.getCredits()))
            return true;
        if (!Objects.equals(normalize(request.getFrequencyEng()), normalize(mv.getFrequencyEng())))
            return true;
        if (!Objects.equals(request.getHoursLecture(), mv.getHoursLecture()))
            return true;
        if (!Objects.equals(request.getHoursExercise(), mv.getHoursExercise()))
            return true;
        if (!Objects.equals(request.getHoursPractical(), mv.getHoursPractical()))
            return true;
        if (!Objects.equals(request.getHoursSeminar(), mv.getHoursSeminar()))
            return true;
        if (!Objects.equals(normalize(request.getFirstSemesterAvailable()), normalize(mv.getFirstSemesterAvailable())))
            return true;
        if (!Objects.equals(normalize(request.getSuccessorModuleName()), normalize(mv.getSuccessorModuleName())))
            return true;
        if (!Objects.equals(request.getLanguageEng(), mv.getLanguageEng()))
            return true;

        Set<String> requestAssignments = assignmentKeySetFromRequest(request.getDegreeProgramAssignments());
        Set<String> mvAssignments = assignmentKeySetFromMv(mv);
        return !requestAssignments.equals(mvAssignments);
    }

    /**
     * True when any module field outside step 1 (basic + assignment set) differs from the
     * persisted version. Used to invalidate examination board feedback when curriculum/content
     * changes without altering step 1.
     */
    public static boolean isPostStep1DataChanged(ModuleVersionUpdateRequestDTO request, ModuleVersion mv) {
        if (!Objects.equals(normalize(request.getBulletPoints()), normalize(mv.getBulletPoints()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getLevelEng()), normalize(mv.getLevelEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getDuration()), normalize(mv.getDuration()))) {
            return true;
        }
        if (!Objects.equals(request.getHoursTotal(), mv.getHoursTotal())) {
            return true;
        }
        if (!Objects.equals(request.getHoursSelfStudy(), mv.getHoursSelfStudy())) {
            return true;
        }
        if (!Objects.equals(request.getHoursPresence(), mv.getHoursPresence())) {
            return true;
        }
        if (!Objects.equals(normalize(request.getExaminationAchievementsEng()),
                normalize(mv.getExaminationAchievementsEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getExaminationAchievementsPromptEng()),
                normalize(mv.getExaminationAchievementsPromptEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getRepetitionEng()), normalize(mv.getRepetitionEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getRecommendedPrerequisitesEng()),
                normalize(mv.getRecommendedPrerequisitesEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getContentEng()), normalize(mv.getContentEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getContentPromptEng()), normalize(mv.getContentPromptEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getLearningOutcomesEng()), normalize(mv.getLearningOutcomesEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getLearningOutcomesPromptEng()),
                normalize(mv.getLearningOutcomesPromptEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getTeachingMethodsEng()), normalize(mv.getTeachingMethodsEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getTeachingMethodsPromptEng()),
                normalize(mv.getTeachingMethodsPromptEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getMediaEng()), normalize(mv.getMediaEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getLiteratureEng()), normalize(mv.getLiteratureEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getResponsiblesEng()), normalize(mv.getResponsiblesEng()))) {
            return true;
        }
        if (!Objects.equals(normalize(request.getLvSwsLecturerEng()), normalize(mv.getLvSwsLecturerEng()))) {
            return true;
        }
        return false;
    }

    private static String normalize(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Set<String> assignmentKeySetFromRequest(List<ModuleDegreeProgramAssignmentDTO> assignments) {
        Set<String> set = new HashSet<>();
        if (assignments == null) {
            return set;
        }
        for (ModuleDegreeProgramAssignmentDTO a : assignments) {
            if (a == null || a.getDegreeProgramId() == null || a.getDegreeProgramSpecializationId() == null) {
                continue;
            }
            set.add(a.getDegreeProgramId() + "," + a.getDegreeProgramSpecializationId());
        }
        return set;
    }

    private static Set<String> assignmentKeySetFromMv(ModuleVersion mv) {
        Set<String> set = new HashSet<>();
        if (mv.getDegreeProgramAssignments() == null) {
            return set;
        }
        for (var a : mv.getDegreeProgramAssignments()) {
            if (a == null || a.getDegreeProgram() == null || a.getDegreeProgramSpecialization() == null) {
                continue;
            }
            set.add(a.getDegreeProgram().getDegreeProgramId() + ","
                    + a.getDegreeProgramSpecialization().getDegreeProgramSpecializationId());
        }
        return set;
    }
}
