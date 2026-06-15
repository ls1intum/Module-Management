package modulemanagement.ls1.shared;

import modulemanagement.ls1.dtos.ProposalAiReviewDTO;
import modulemanagement.ls1.dtos.ProposalAiReviewSectionDTO;
import modulemanagement.ls1.enums.AiReviewSeverity;
import modulemanagement.ls1.enums.ProposalReviewSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProposalAiReviewParserTest {

    @Test
    void parsesValidJsonResponse() {
        String response = """
                {
                  "summary": "Solid proposal with minor gaps.",
                  "sections": [
                    {"section": "CONTENT", "severity": "OK", "findings": "Content is well structured.", "suggestions": ""},
                    {"section": "CREDITS", "severity": "CRITICAL", "findings": "Credits missing.", "suggestions": "Add ECTS credits."}
                  ]
                }
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals("Solid proposal with minor gaps.", dto.getSummary());
        assertEquals(2, dto.getSections().size());
        // Sorted by severity: CRITICAL first
        assertEquals(ProposalReviewSection.CREDITS, dto.getSections().get(0).getSection());
        assertEquals(AiReviewSeverity.CRITICAL, dto.getSections().get(0).getSeverity());
        assertEquals("Add ECTS credits.", dto.getSections().get(0).getSuggestions());
        assertEquals(ProposalReviewSection.CONTENT, dto.getSections().get(1).getSection());
        assertEquals(AiReviewSeverity.OK, dto.getSections().get(1).getSeverity());
    }

    @Test
    void parsesJsonWrappedInMarkdownCodeFence() {
        String response = """
                Here is the review:
                ```json
                {"summary": "Looks good.", "sections": [{"section": "TITLE_ENG", "severity": "ATTENTION", "findings": "Too vague.", "suggestions": "Be specific."}]}
                ```
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals("Looks good.", dto.getSummary());
        assertEquals(1, dto.getSections().size());
        assertEquals(ProposalReviewSection.TITLE_ENG, dto.getSections().get(0).getSection());
    }

    @Test
    void skipsUnknownSectionNames() {
        String response = """
                {"summary": "S", "sections": [
                  {"section": "NOT_A_REAL_SECTION", "severity": "OK", "findings": "x", "suggestions": ""},
                  {"section": "MEDIA", "severity": "OK", "findings": "fine", "suggestions": ""}
                ]}
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals(1, dto.getSections().size());
        assertEquals(ProposalReviewSection.MEDIA, dto.getSections().get(0).getSection());
    }

    @Test
    void acceptsLowercaseSectionAndSeverity() {
        String response = """
                {"summary": "S", "sections": [{"section": "content", "severity": "critical", "findings": "x", "suggestions": "y"}]}
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals(1, dto.getSections().size());
        assertEquals(ProposalReviewSection.CONTENT, dto.getSections().get(0).getSection());
        assertEquals(AiReviewSeverity.CRITICAL, dto.getSections().get(0).getSeverity());
    }

    @Test
    void fallsBackToAttentionForInvalidSeverity() {
        String response = """
                {"summary": "S", "sections": [{"section": "CONTENT", "severity": "BANANAS", "findings": "x", "suggestions": ""}]}
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals(AiReviewSeverity.ATTENTION, dto.getSections().get(0).getSeverity());
    }

    @Test
    void usesRawResponseAsSummaryWhenNotJson() {
        String response = "Sorry, I cannot produce JSON right now.";

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals(response, dto.getSummary());
        assertTrue(dto.getSections().isEmpty());
    }

    @Test
    void handlesNullAndEmptyResponses() {
        assertEquals(ProposalAiReviewParser.UNPARSEABLE_SUMMARY, ProposalAiReviewParser.parse(null).getSummary());
        assertEquals(ProposalAiReviewParser.UNPARSEABLE_SUMMARY, ProposalAiReviewParser.parse("  ").getSummary());
        assertTrue(ProposalAiReviewParser.parse(null).getSections().isEmpty());
    }

    @Test
    void usesFallbackSummaryWhenSummaryMissing() {
        String response = """
                {"sections": [{"section": "CONTENT", "severity": "OK", "findings": "fine", "suggestions": ""}]}
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals(ProposalAiReviewParser.FALLBACK_SUMMARY, dto.getSummary());
        assertEquals(1, dto.getSections().size());
    }

    @Test
    void sortsSectionsBySeverityCriticalFirst() {
        String response = """
                {"summary": "S", "sections": [
                  {"section": "MEDIA", "severity": "OK", "findings": "a", "suggestions": ""},
                  {"section": "CONTENT", "severity": "ATTENTION", "findings": "b", "suggestions": ""},
                  {"section": "CREDITS", "severity": "CRITICAL", "findings": "c", "suggestions": ""}
                ]}
                """;

        List<ProposalAiReviewSectionDTO> sections = ProposalAiReviewParser.parse(response).getSections();

        assertEquals(AiReviewSeverity.CRITICAL, sections.get(0).getSeverity());
        assertEquals(AiReviewSeverity.ATTENTION, sections.get(1).getSeverity());
        assertEquals(AiReviewSeverity.OK, sections.get(2).getSeverity());
    }

    @Test
    void setsSectionLabelFromSectionEnum() {
        String response = """
                {"summary": "S", "sections": [{"section": "LEARNING_OUTCOMES", "severity": "OK", "findings": "x", "suggestions": ""}]}
                """;

        ProposalAiReviewDTO dto = ProposalAiReviewParser.parse(response);

        assertEquals("Learning outcomes", dto.getSections().get(0).getSectionLabel());
    }
}
