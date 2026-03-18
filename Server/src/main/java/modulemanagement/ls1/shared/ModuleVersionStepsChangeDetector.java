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
