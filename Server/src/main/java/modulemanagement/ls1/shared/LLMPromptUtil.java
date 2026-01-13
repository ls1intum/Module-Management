package modulemanagement.ls1.shared;

import modulemanagement.ls1.dtos.CompletionServiceRequestDTO;

import java.util.ArrayList;
import java.util.List;

public final class LLMPromptUtil {

    public static String buildPrompt(String field, CompletionServiceRequestDTO request) {
        String context = accumulateModuleInfoFor(field, request);
        return getPromptFor(field,
                request.getBulletPoints(),
                request.getContextPrompt() != null ? request.getContextPrompt() : "",
                context);
    }

    public static String accumulateModuleInfoFor(String field, CompletionServiceRequestDTO moduleInfo) {
        List<String> contextParts = new ArrayList<>();

        if (moduleInfo.getTitleEng() != null && !moduleInfo.getTitleEng().isEmpty()) {
            contextParts.add("Module Title: " + moduleInfo.getTitleEng());
        }
        if (moduleInfo.getLevelEng() != null && !moduleInfo.getLevelEng().isEmpty()) {
            contextParts.add("Level: " + moduleInfo.getLevelEng());
        }
        if (moduleInfo.getLanguageEng() != null) {
            contextParts.add("Language: " + moduleInfo.getLanguageEng());
        }
        if (moduleInfo.getFrequencyEng() != null && !moduleInfo.getFrequencyEng().isEmpty()) {
            contextParts.add("Frequency: " + moduleInfo.getFrequencyEng());
        }
        if (moduleInfo.getCredits() != null) {
            contextParts.add("Credits: " + moduleInfo.getCredits());
        }
        if (moduleInfo.getDurationEng() != null && !moduleInfo.getDurationEng().isEmpty()) {
            contextParts.add("Duration: " + moduleInfo.getDurationEng());
        }
        if (moduleInfo.getHoursTotal() != null) {
            contextParts.add("Total Hours: " + moduleInfo.getHoursTotal());
        }
        if (moduleInfo.getHoursSelfStudy() != null) {
            contextParts.add("Self Study Hours: " + moduleInfo.getHoursSelfStudy());
        }
        if (moduleInfo.getHoursPresence() != null) {
            contextParts.add("Presence Hours: " + moduleInfo.getHoursPresence());
        }
        if (moduleInfo.getExaminationAchievementsEng() != null &&
                !moduleInfo.getExaminationAchievementsEng().isEmpty() &&
                !field.equals("examination-achievements")) {
            contextParts.add("Examination Achievements: " + moduleInfo.getExaminationAchievementsEng());
        }
        if (moduleInfo.getRepetitionEng() != null && !moduleInfo.getRepetitionEng().isEmpty()) {
            contextParts.add("Repetition: " + moduleInfo.getRepetitionEng());
        }
        if (moduleInfo.getRecommendedPrerequisitesEng() != null &&
                !moduleInfo.getRecommendedPrerequisitesEng().isEmpty()) {
            contextParts.add("Recommended Prerequisites: " + moduleInfo.getRecommendedPrerequisitesEng());
        }
        if (moduleInfo.getContentEng() != null &&
                !moduleInfo.getContentEng().isEmpty() &&
                !field.equals("content")) {
            contextParts.add("Current Content: " + moduleInfo.getContentEng());
        }
        if (moduleInfo.getLearningOutcomesEng() != null &&
                !moduleInfo.getLearningOutcomesEng().isEmpty() &&
                !field.equals("learning-outcomes")) {
            contextParts.add("Learning Outcomes: " + moduleInfo.getLearningOutcomesEng());
        }
        if (moduleInfo.getTeachingMethodsEng() != null &&
                !moduleInfo.getTeachingMethodsEng().isEmpty() &&
                !field.equals("teaching-methods")) {
            contextParts.add("Teaching Methods: " + moduleInfo.getTeachingMethodsEng());
        }
        if (moduleInfo.getMediaEng() != null &&
                !moduleInfo.getMediaEng().isEmpty() &&
                !field.equals("media")) {
            contextParts.add("Media: " + moduleInfo.getMediaEng());
        }
        if (moduleInfo.getLiteratureEng() != null && !moduleInfo.getLiteratureEng().isEmpty()) {
            contextParts.add("Literature: " + moduleInfo.getLiteratureEng());
        }
        if (moduleInfo.getResponsiblesEng() != null && !moduleInfo.getResponsiblesEng().isEmpty()) {
            contextParts.add("Responsibles: " + moduleInfo.getResponsiblesEng());
        }
        if (moduleInfo.getLvSwsLecturerEng() != null && !moduleInfo.getLvSwsLecturerEng().isEmpty()) {
            contextParts.add("LV SWS Lecturer: " + moduleInfo.getLvSwsLecturerEng());
        }

        return String.join(" | ", contextParts);
    }

    public static String getPromptFor(String field, String bulletPoints, String contextPrompt, String context) {
        return String.format(
                "You are a professor at the Technical University of Munich and an expert in creating academic module descriptions.\n"
                        +
                        "Given the following information about a module:\n\n" +
                        "%s\n\n" +
                        "And these key points about the module:\n" +
                        "%s\n\n" +
                        "Generate a comprehensive, well-structured description of the module's %s.\n" +
                        "The description should:\n" +
                        "- Be written in clear academic English\n" +
                        "- Be detailed but concise\n" +
                        "- Within 50-200 words but as little as necessary\n" +
                        "- Follow a logical structure\n" +
                        "- Maintain a professional tone\n" +
                        "- Be suitable for a university module catalog\n" +
                        "- Never mention Credits, Hours, Frequency, Duration unless it is asked for in the specific context\n\n"
                        +
                        "Consider all information, but without breaking the establised rules, specifically consider the following context:\n\n"
                        +
                        "%s\n\n" +
                        "Generate only the %s description, without any additional comments, explanations, or prepending the data with the name of the field.",
                context,
                bulletPoints != null ? bulletPoints : "",
                field,
                contextPrompt != null ? contextPrompt : "",
                field);
    }
}