package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.ProposalReviewSection;
import modulemanagement.ls1.models.AiReviewGuideline;
import modulemanagement.ls1.models.ModuleVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProposalAiReviewPromptUtilTest {

    private ModuleVersion sampleModuleVersion() {
        ModuleVersion mv = new ModuleVersion();
        mv.setTitleEng("Advanced Databases");
        mv.setCredits(6);
        mv.setContentEng("Query optimization, transactions, distributed storage.");
        return mv;
    }

    private AiReviewGuideline guideline(ProposalReviewSection section, String title, String instruction) {
        AiReviewGuideline g = new AiReviewGuideline();
        g.setSection(section);
        g.setTitle(title);
        g.setInstruction(instruction);
        return g;
    }

    @Test
    void promptContainsFilledProposalFields() {
        String prompt = ProposalAiReviewPromptUtil.buildReviewPrompt(sampleModuleVersion(), List.of());

        assertTrue(prompt.contains("Advanced Databases"));
        assertTrue(prompt.contains("6"));
        assertTrue(prompt.contains("Query optimization"));
    }

    @Test
    void promptOmitsEmptyFields() {
        ModuleVersion mv = new ModuleVersion();
        mv.setTitleEng("Only Title");

        String prompt = ProposalAiReviewPromptUtil.buildReviewPrompt(mv, List.of());

        assertTrue(prompt.contains("Only Title"));
        assertFalse(prompt.contains("Learning outcomes:\n"));
    }

    @Test
    void promptUsesBalancedReviewInstruction() {
        String prompt = ProposalAiReviewPromptUtil.buildReviewPrompt(sampleModuleVersion(), List.of());

        assertTrue(prompt.contains("authors improving the proposal"));
        assertTrue(prompt.contains("reviewers assessing it"));
    }

    @Test
    void generalGuidelinesAreIncludedInBasePrompt() {
        AiReviewGuideline general = guideline(ProposalReviewSection.GENERAL, "Tone", "Use academic English.");

        String prompt = ProposalAiReviewPromptUtil.buildReviewPrompt(sampleModuleVersion(), List.of(general));

        assertTrue(prompt.contains("Tone"));
        assertTrue(prompt.contains("Use academic English."));
    }

    @Test
    void missingGuidelinesAreMarkedInPrompt() {
        String prompt = ProposalAiReviewPromptUtil.buildReviewPrompt(sampleModuleVersion(), List.of());

        assertTrue(prompt.contains("No general guidelines configured"));
    }

    @Test
    void appendSectionGuidelinesAddsSectionSpecificRules() {
        AiReviewGuideline contentRule = guideline(ProposalReviewSection.CONTENT, "Depth",
                "Describe at least three thematic blocks.");
        String base = "BASE";

        String result = ProposalAiReviewPromptUtil.appendSectionGuidelines(base, List.of(contentRule));

        assertTrue(result.startsWith("BASE"));
        assertTrue(result.contains("Depth"));
        assertTrue(result.contains("three thematic blocks"));
    }

    @Test
    void appendSectionGuidelinesWithNoRulesReturnsBase() {
        String result = ProposalAiReviewPromptUtil.appendSectionGuidelines("BASE", List.of());

        assertEquals("BASE", result);
    }

    @Test
    void promptListsAllowedSectionEnumNames() {
        String prompt = ProposalAiReviewPromptUtil.buildReviewPrompt(sampleModuleVersion(), List.of());

        assertTrue(prompt.contains("CONTENT"));
        assertTrue(prompt.contains("LEARNING_OUTCOMES"));
        assertFalse(prompt.contains("Allowed section enum values: GENERAL"));
    }
}
