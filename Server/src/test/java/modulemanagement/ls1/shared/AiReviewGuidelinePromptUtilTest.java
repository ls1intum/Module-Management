package modulemanagement.ls1.shared;

import modulemanagement.ls1.enums.Language;
import modulemanagement.ls1.enums.ProposalReviewSection;
import modulemanagement.ls1.models.AiReviewGuideline;
import modulemanagement.ls1.models.ModuleVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiReviewGuidelinePromptUtilTest {

    private AiReviewGuideline guideline(ProposalReviewSection section, String title, String instruction) {
        AiReviewGuideline g = new AiReviewGuideline();
        g.setSection(section);
        g.setTitle(title);
        g.setInstruction(instruction);
        return g;
    }

    @Test
    void extractsStringAndNumericAndEnumFields() {
        ModuleVersion mv = new ModuleVersion();
        mv.setTitleEng("Databases");
        mv.setCredits(6);
        mv.setLanguageEng(Language.English);

        assertEquals("Databases", AiReviewGuidelinePromptUtil.extractFieldValue(mv, ProposalReviewSection.TITLE_ENG));
        assertEquals("6", AiReviewGuidelinePromptUtil.extractFieldValue(mv, ProposalReviewSection.CREDITS));
        assertEquals("English", AiReviewGuidelinePromptUtil.extractFieldValue(mv, ProposalReviewSection.LANGUAGE_ENG));
    }

    @Test
    void returnsNullForEmptyOrMissingValues() {
        ModuleVersion mv = new ModuleVersion();
        mv.setTitleEng("   ");

        assertNull(AiReviewGuidelinePromptUtil.extractFieldValue(mv, ProposalReviewSection.TITLE_ENG));
        assertNull(AiReviewGuidelinePromptUtil.extractFieldValue(mv, ProposalReviewSection.CONTENT));
        assertNull(AiReviewGuidelinePromptUtil.extractFieldValue(null, ProposalReviewSection.CONTENT));
        assertNull(AiReviewGuidelinePromptUtil.extractFieldValue(mv, null));
    }

    @Test
    void formatsGuidelinesForMatchingSectionOnly() {
        List<AiReviewGuideline> guidelines = List.of(
                guideline(ProposalReviewSection.CONTENT, "Depth", "Three thematic blocks."),
                guideline(ProposalReviewSection.MEDIA, "Media rule", "List used media."));

        String formatted = AiReviewGuidelinePromptUtil.formatGuidelinesForSection(guidelines,
                ProposalReviewSection.CONTENT);

        assertTrue(formatted.contains("Depth"));
        assertTrue(formatted.contains("Three thematic blocks."));
        assertFalse(formatted.contains("Media rule"));
    }

    @Test
    void returnsEmptyStringWhenNoGuidelinesMatch() {
        List<AiReviewGuideline> guidelines = List.of(
                guideline(ProposalReviewSection.MEDIA, "Media rule", "List used media."));

        assertEquals("", AiReviewGuidelinePromptUtil.formatGuidelinesForSection(guidelines,
                ProposalReviewSection.CONTENT));
        assertEquals("", AiReviewGuidelinePromptUtil.formatGuidelinesForSection(List.of(),
                ProposalReviewSection.CONTENT));
        assertEquals("", AiReviewGuidelinePromptUtil.formatGuidelinesForSection(null,
                ProposalReviewSection.CONTENT));
    }

    @Test
    void everySectionHasALabel() {
        for (ProposalReviewSection section : ProposalReviewSection.values()) {
            String label = AiReviewGuidelinePromptUtil.getSectionLabel(section);
            assertNotNull(label);
            assertFalse(label.isBlank());
        }
    }
}
