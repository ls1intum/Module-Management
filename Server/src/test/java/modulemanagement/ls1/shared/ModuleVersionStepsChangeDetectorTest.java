package modulemanagement.ls1.shared;

import modulemanagement.ls1.dtos.ModuleDegreeProgramAssignmentDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.models.DegreeProgram;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.ModuleVersionDegreeProgramAssignment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModuleVersionStepsChangeDetectorTest {

    @Test
    void step1UnchangedWhenBasicFieldsAndAssignmentsMatch() {
        ModuleVersion mv = baselineVersion();
        ModuleVersionUpdateRequestDTO request = requestMatching(mv);

        assertFalse(ModuleVersionStepsChangeDetector.isStep1DataChanged(request, mv));
        assertFalse(ModuleVersionStepsChangeDetector.isPostStep1DataChanged(request, mv));
    }

    @Test
    void step1DetectsTitleChangeAndIgnoresWhitespaceNormalization() {
        ModuleVersion mv = baselineVersion();
        ModuleVersionUpdateRequestDTO same = requestMatching(mv);
        same.setTitleEng("  Advanced Database Systems  ");
        assertFalse(ModuleVersionStepsChangeDetector.isStep1DataChanged(same, mv));

        ModuleVersionUpdateRequestDTO changed = requestMatching(mv);
        changed.setTitleEng("Advanced Databases");
        assertTrue(ModuleVersionStepsChangeDetector.isStep1DataChanged(changed, mv));
        assertFalse(ModuleVersionStepsChangeDetector.isPostStep1DataChanged(changed, mv));
    }

    @Test
    void step1DetectsAssignmentSetChange() {
        ModuleVersion mv = baselineVersion();
        ModuleVersionUpdateRequestDTO request = requestMatching(mv);
        ModuleDegreeProgramAssignmentDTO other = new ModuleDegreeProgramAssignmentDTO();
        other.setDegreeProgramId(1L);
        other.setDegreeProgramSpecializationId(99L);
        request.setDegreeProgramAssignments(List.of(other));

        assertTrue(ModuleVersionStepsChangeDetector.isStep1DataChanged(request, mv));
    }

    @Test
    void postStep1DetectsContentChangeWithoutStep1Change() {
        ModuleVersion mv = baselineVersion();
        ModuleVersionUpdateRequestDTO request = requestMatching(mv);
        request.setContentEng("Revised query-processing chapter.");

        assertFalse(ModuleVersionStepsChangeDetector.isStep1DataChanged(request, mv));
        assertTrue(ModuleVersionStepsChangeDetector.isPostStep1DataChanged(request, mv));
    }

    @Test
    void postStep1DetectsLearningOutcomesChange() {
        ModuleVersion mv = baselineVersion();
        ModuleVersionUpdateRequestDTO request = requestMatching(mv);
        request.setLearningOutcomesEng("Students can optimize buffer managers.");

        assertTrue(ModuleVersionStepsChangeDetector.isPostStep1DataChanged(request, mv));
        assertFalse(ModuleVersionStepsChangeDetector.isStep1DataChanged(request, mv));
    }

    @Test
    void emptyAndNullStringsNormalizeAsEqual() {
        ModuleVersion mv = baselineVersion();
        mv.setSuccessorModuleName(null);
        ModuleVersionUpdateRequestDTO request = requestMatching(mv);
        request.setSuccessorModuleName("   ");

        assertFalse(ModuleVersionStepsChangeDetector.isStep1DataChanged(request, mv));
    }

    private static ModuleVersion baselineVersion() {
        ModuleVersion mv = new ModuleVersion();
        mv.setTitleEng("Advanced Database Systems");
        mv.setTitleDe("Fortgeschrittene Datenbanksysteme");
        mv.setCredits(5);
        mv.setFrequencyEng("Winter semester");
        mv.setHoursLecture(2);
        mv.setHoursExercise(2);
        mv.setHoursPractical(0);
        mv.setHoursSeminar(0);
        mv.setFirstSemesterAvailable("WS2024");
        mv.setSuccessorModuleName(null);
        mv.setLanguageEng(Language.English);
        mv.setBulletPoints("- indexing");
        mv.setLevelEng("Master");
        mv.setDuration("1");
        mv.setHoursTotal(150);
        mv.setHoursSelfStudy(90);
        mv.setHoursPresence(60);
        mv.setExaminationAchievementsEng("Written exam");
        mv.setExaminationAchievementsPromptEng("");
        mv.setRepetitionEng("Next semester");
        mv.setRecommendedPrerequisitesEng("Intro DB");
        mv.setContentEng("Query processing and indexing");
        mv.setContentPromptEng("");
        mv.setLearningOutcomesEng("Analyze DB internals");
        mv.setLearningOutcomesPromptEng("");
        mv.setTeachingMethodsEng("Lecture");
        mv.setTeachingMethodsPromptEng("");
        mv.setMediaEng("Slides");
        mv.setLiteratureEng("Ramakrishnan");
        mv.setResponsiblesEng("Prof Example");
        mv.setLvSwsLecturerEng("2V+2U");

        DegreeProgram program = new DegreeProgram();
        program.setDegreeProgramId(1L);
        DegreeProgramSpecialization spec = new DegreeProgramSpecialization();
        spec.setDegreeProgramSpecializationId(2L);
        ModuleVersionDegreeProgramAssignment assignment = new ModuleVersionDegreeProgramAssignment();
        assignment.setDegreeProgram(program);
        assignment.setDegreeProgramSpecialization(spec);
        mv.setDegreeProgramAssignments(new ArrayList<>(List.of(assignment)));
        return mv;
    }

    private static ModuleVersionUpdateRequestDTO requestMatching(ModuleVersion mv) {
        ModuleVersionUpdateRequestDTO request = new ModuleVersionUpdateRequestDTO();
        request.setTitleEng(mv.getTitleEng());
        request.setTitleDe(mv.getTitleDe());
        request.setCredits(mv.getCredits());
        request.setFrequencyEng(mv.getFrequencyEng());
        request.setHoursLecture(mv.getHoursLecture());
        request.setHoursExercise(mv.getHoursExercise());
        request.setHoursPractical(mv.getHoursPractical());
        request.setHoursSeminar(mv.getHoursSeminar());
        request.setFirstSemesterAvailable(mv.getFirstSemesterAvailable());
        request.setSuccessorModuleName(mv.getSuccessorModuleName());
        request.setLanguageEng(mv.getLanguageEng());
        request.setBulletPoints(mv.getBulletPoints());
        request.setLevelEng(mv.getLevelEng());
        request.setDuration(mv.getDuration());
        request.setHoursTotal(mv.getHoursTotal());
        request.setHoursSelfStudy(mv.getHoursSelfStudy());
        request.setHoursPresence(mv.getHoursPresence());
        request.setExaminationAchievementsEng(mv.getExaminationAchievementsEng());
        request.setExaminationAchievementsPromptEng(mv.getExaminationAchievementsPromptEng());
        request.setRepetitionEng(mv.getRepetitionEng());
        request.setRecommendedPrerequisitesEng(mv.getRecommendedPrerequisitesEng());
        request.setContentEng(mv.getContentEng());
        request.setContentPromptEng(mv.getContentPromptEng());
        request.setLearningOutcomesEng(mv.getLearningOutcomesEng());
        request.setLearningOutcomesPromptEng(mv.getLearningOutcomesPromptEng());
        request.setTeachingMethodsEng(mv.getTeachingMethodsEng());
        request.setTeachingMethodsPromptEng(mv.getTeachingMethodsPromptEng());
        request.setMediaEng(mv.getMediaEng());
        request.setLiteratureEng(mv.getLiteratureEng());
        request.setResponsiblesEng(mv.getResponsiblesEng());
        request.setLvSwsLecturerEng(mv.getLvSwsLecturerEng());

        ModuleDegreeProgramAssignmentDTO assignment = new ModuleDegreeProgramAssignmentDTO();
        assignment.setDegreeProgramId(1L);
        assignment.setDegreeProgramSpecializationId(2L);
        request.setDegreeProgramAssignments(List.of(assignment));
        return request;
    }
}
