package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.ProposalReviewSection;
import modulemanagement.ls1.models.AiReviewGuideline;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.ModuleVersionDegreeProgramAssignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ProposalAiReviewPromptUtil {

    private static final List<ProposalReviewSection> REVIEWABLE_SECTIONS = Arrays.stream(ProposalReviewSection.values())
            .filter(s -> s != ProposalReviewSection.GENERAL)
            .toList();

    private ProposalAiReviewPromptUtil() {
    }

    private static final String REVIEW_INSTRUCTION = """
            Provide an objective academic review suitable for both authors improving the proposal \
            and reviewers assessing it. Highlight strengths, gaps, compliance issues, and actionable \
            improvements. Flag blockers as CRITICAL.""";

    public static String buildReviewPrompt(ModuleVersion moduleVersion, List<AiReviewGuideline> guidelines) {
        String generalGuidelines = AiReviewGuidelinePromptUtil.formatGuidelinesForSection(guidelines,
                ProposalReviewSection.GENERAL);

        return """
                You are an expert academic module proposal reviewer at the Technical University of Munich (TUM).
                %s

                Review the module proposal below against the provided guidelines.
                Only evaluate sections that have submitted content and/or applicable guidelines.
                Be concise but thorough. Flag missing critical information as CRITICAL.

                === MODULE PROPOSAL ===
                %s

                === REVIEW GUIDELINES (apply all relevant rules) ===
                %s

                === RESPONSE FORMAT ===
                Respond with valid JSON only (no markdown fences), using this exact structure:
                {
                  "summary": "2-4 sentence overall assessment",
                  "sections": [
                    {
                      "section": "SECTION_ENUM_NAME",
                      "severity": "OK|ATTENTION|CRITICAL",
                      "findings": "what you observed",
                      "suggestions": "actionable advice (empty string if severity is OK)"
                    }
                  ]
                }

                Allowed section enum values: %s
                Use severity OK when the section meets guidelines; ATTENTION for improvable issues; CRITICAL for blockers or major gaps.
                """
                .formatted(
                        REVIEW_INSTRUCTION,
                        buildProposalDataBlock(moduleVersion),
                        generalGuidelines.isEmpty() ? "(No general guidelines configured.)" : generalGuidelines,
                        REVIEWABLE_SECTIONS.stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    private static String buildProposalDataBlock(ModuleVersion mv) {
        List<String> parts = new ArrayList<>();
        for (ProposalReviewSection section : REVIEWABLE_SECTIONS) {
            if (section == ProposalReviewSection.DEGREE_PROGRAM_ASSIGNMENTS) {
                String assignments = formatDegreeProgramAssignments(mv);
                if (!assignments.isBlank()) {
                    parts.add(AiReviewGuidelinePromptUtil.getSectionLabel(section) + ":\n" + assignments);
                }
                continue;
            }
            String value = AiReviewGuidelinePromptUtil.extractFieldValue(mv, section);
            if (value != null) {
                parts.add(AiReviewGuidelinePromptUtil.getSectionLabel(section) + ":\n" + value);
            }
        }
        if (parts.isEmpty()) {
            return "(No proposal fields filled in yet.)";
        }
        return String.join("\n\n", parts);
    }

    private static String formatDegreeProgramAssignments(ModuleVersion mv) {
        if (mv.getDegreeProgramAssignments() == null || mv.getDegreeProgramAssignments().isEmpty()) {
            return "";
        }
        return mv.getDegreeProgramAssignments().stream()
                .map(ProposalAiReviewPromptUtil::formatAssignment)
                .collect(Collectors.joining("\n"));
    }

    private static String formatAssignment(ModuleVersionDegreeProgramAssignment a) {
        String program = a.getDegreeProgram() != null ? a.getDegreeProgram().getName() : "Unknown program";
        String specialization = a.getDegreeProgramSpecialization() != null
                ? a.getDegreeProgramSpecialization().getName()
                : "Unknown specialization";
        return "- " + program + " / " + specialization;
    }

    public static String appendSectionGuidelines(String basePrompt, List<AiReviewGuideline> guidelines) {
        StringBuilder sb = new StringBuilder(basePrompt);
        for (ProposalReviewSection section : REVIEWABLE_SECTIONS) {
            String sectionRules = AiReviewGuidelinePromptUtil.formatGuidelinesForSection(guidelines, section);
            if (!sectionRules.isEmpty()) {
                sb.append("\n\n").append(sectionRules);
            }
        }
        return sb.toString();
    }
}
