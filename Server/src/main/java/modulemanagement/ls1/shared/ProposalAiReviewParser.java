package modulemanagement.ls1.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import modulemanagement.ls1.dtos.ProposalAiReviewDTO;
import modulemanagement.ls1.dtos.ProposalAiReviewSectionDTO;
import modulemanagement.ls1.enums.AiReviewSeverity;
import modulemanagement.ls1.enums.ProposalReviewSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parses the LLM JSON response of a proposal review into a {@link ProposalAiReviewDTO}.
 * Tolerates markdown code fences, surrounding prose, unknown section names, and
 * missing/invalid severities. Falls back to using the raw response as summary when
 * no JSON object can be parsed at all.
 */
public final class ProposalAiReviewParser {

    public static final String FALLBACK_SUMMARY = "Review completed. See section details below.";
    public static final String UNPARSEABLE_SUMMARY = "Review could not be parsed.";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ProposalAiReviewParser() {
    }

    public static ProposalAiReviewDTO parse(String llmResponse) {
        ProposalAiReviewDTO dto = new ProposalAiReviewDTO();
        if (llmResponse == null || llmResponse.isBlank()) {
            dto.setSummary(UNPARSEABLE_SUMMARY);
            dto.setSections(List.of());
            return dto;
        }
        try {
            String json = extractJson(llmResponse);
            JsonNode root = OBJECT_MAPPER.readTree(json);
            if (!root.isObject()) {
                throw new IllegalArgumentException("Response is not a JSON object");
            }
            dto.setSummary(textOrDefault(root.get("summary"), FALLBACK_SUMMARY));
            dto.setSections(parseSections(root.get("sections")));
        } catch (Exception e) {
            String raw = llmResponse != null ? llmResponse.trim() : "";
            dto.setSummary(raw.isEmpty() ? UNPARSEABLE_SUMMARY : raw);
            dto.setSections(List.of());
        }
        return dto;
    }

    private static List<ProposalAiReviewSectionDTO> parseSections(JsonNode sectionsNode) {
        List<ProposalAiReviewSectionDTO> sections = new ArrayList<>();
        if (sectionsNode == null || !sectionsNode.isArray()) {
            return sections;
        }
        for (JsonNode node : sectionsNode) {
            ProposalAiReviewSectionDTO section = parseSectionNode(node);
            if (section != null) {
                sections.add(section);
            }
        }
        sections.sort(Comparator.comparingInt(s -> severityOrder(s.getSeverity())));
        return sections;
    }

    private static ProposalAiReviewSectionDTO parseSectionNode(JsonNode node) {
        if (node == null || !node.has("section")) {
            return null;
        }
        ProposalAiReviewSectionDTO section = new ProposalAiReviewSectionDTO();
        try {
            section.setSection(ProposalReviewSection.valueOf(node.get("section").asText().trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return null;
        }
        section.setSectionLabel(AiReviewGuidelinePromptUtil.getSectionLabel(section.getSection()));
        section.setSeverity(parseSeverity(node.get("severity")));
        section.setFindings(textOrDefault(node.get("findings"), ""));
        section.setSuggestions(textOrDefault(node.get("suggestions"), ""));
        return section;
    }

    private static AiReviewSeverity parseSeverity(JsonNode node) {
        if (node == null || node.isNull()) {
            return AiReviewSeverity.ATTENTION;
        }
        try {
            return AiReviewSeverity.valueOf(node.asText().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiReviewSeverity.ATTENTION;
        }
    }

    private static String textOrDefault(JsonNode node, String defaultValue) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        String text = node.asText().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    /**
     * Extracts the outermost JSON object from a response that may contain code fences
     * or surrounding prose.
     */
    static String extractJson(String response) {
        if (response == null) {
            return "{}";
        }
        String trimmed = response.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    public static int severityOrder(AiReviewSeverity severity) {
        if (severity == AiReviewSeverity.CRITICAL) {
            return 0;
        }
        if (severity == AiReviewSeverity.ATTENTION) {
            return 1;
        }
        return 2;
    }
}
