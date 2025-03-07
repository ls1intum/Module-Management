package modulemanagement.ls1.shared;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfCreator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Resource createModuleVersionPdf(ModuleVersion mv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            // Title
            Paragraph title = new Paragraph("Module Description: " + mv.getTitleEng())
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Metadata section
            document.add(new Paragraph("Module Information").setFontSize(14).setMarginTop(20));
            Table metadataTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100));

            addTableRow(metadataTable, "Module ID", mv.getModuleId() != null ? mv.getModuleId() : "Not assigned yet");
            addTableRow(metadataTable, "Version", mv.getVersion().toString());
            addTableRow(metadataTable, "Status", mv.getStatus().toString());
            addTableRow(metadataTable, "Creation Date", mv.getCreationDate().format(DATE_FORMATTER));
            addTableRow(metadataTable, "Language", mv.getLanguageEng() != null ? mv.getLanguageEng().toString() : "Not specified");
            addTableRow(metadataTable, "Level", mv.getLevelEng());
            addTableRow(metadataTable, "Credits", mv.getCredits() != null ? mv.getCredits().toString() : "Not specified");
            addTableRow(metadataTable, "Frequency", mv.getFrequencyEng());
            addTableRow(metadataTable, "Duration", mv.getDuration());

            document.add(metadataTable);

            // Hours section
            document.add(new Paragraph("Hours").setFontSize(14).setMarginTop(20));
            Table hoursTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100));

            addTableRow(hoursTable, "Total Hours", mv.getHoursTotal() != null ? mv.getHoursTotal().toString() : "Not specified");
            addTableRow(hoursTable, "Self-Study Hours", mv.getHoursSelfStudy() != null ? mv.getHoursSelfStudy().toString() : "Not specified");
            addTableRow(hoursTable, "Presence Hours", mv.getHoursPresence() != null ? mv.getHoursPresence().toString() : "Not specified");

            document.add(hoursTable);

            // Content sections
            addContentSection(document, "Module Content", mv.getContentEng());
            addContentSection(document, "Learning Outcomes", mv.getLearningOutcomesEng());
            addContentSection(document, "Teaching Methods", mv.getTeachingMethodsEng());
            addContentSection(document, "Examination Achievements", mv.getExaminationAchievementsEng());
            addContentSection(document, "Recommended Prerequisites", mv.getRecommendedPrerequisitesEng());
            addContentSection(document, "Media", mv.getMediaEng());
            addContentSection(document, "Literature", mv.getLiteratureEng());
            addContentSection(document, "Responsibles", mv.getResponsiblesEng());
            addContentSection(document, "LV SWS Lecturer", mv.getLvSwsLecturerEng());

            // Footer
            document.add(new Paragraph("Generated on " + java.time.LocalDate.now().format(DATE_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(30));

            document.close();
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    public Resource createProfessorModuleVersionPdf(ModuleVersion mv) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            // Title
            Paragraph title = new Paragraph("Module Description: " + mv.getTitleEng())
                    .setFontSize(18)

                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Metadata
            Paragraph metadata = new Paragraph(String.format(
                    "Module ID: %s | Version: %d | Status: %s | Created: %s",
                    mv.getModuleId() != null ? mv.getModuleId() : "Not assigned",
                    mv.getVersion(),
                    mv.getStatus(),
                    mv.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(metadata);

            // Basic Information
            document.add(new Paragraph("Basic Information").setFontSize(16).setMarginTop(10));

            // Add fields with specific feedback
            addFieldWithSpecificFeedback(document, "Title", mv.getTitleEng(), mv.getRequiredFeedbacks(), "titleFeedback", "titleAccepted");
            addFieldWithSpecificFeedback(document, "Level", mv.getLevelEng(), mv.getRequiredFeedbacks(), "levelFeedback", "levelAccepted");
            addFieldWithSpecificFeedback(document, "Language", mv.getLanguageEng() != null ? mv.getLanguageEng().toString() : null,
                    mv.getRequiredFeedbacks(), "languageFeedback", "languageAccepted");
            addFieldWithSpecificFeedback(document, "Frequency", mv.getFrequencyEng(), mv.getRequiredFeedbacks(), "frequencyFeedback", "frequencyAccepted");
            addFieldWithSpecificFeedback(document, "Repetition", mv.getRepetitionEng(), mv.getRequiredFeedbacks(), "repetitionFeedback", "repetitionAccepted");
            addFieldWithSpecificFeedback(document, "Duration", mv.getDuration(), mv.getRequiredFeedbacks(), "durationFeedback", "durationAccepted");

            // Hours Section
            document.add(new Paragraph("Hours").setFontSize(16).setMarginTop(10));

            addFieldWithSpecificFeedback(document, "Credits", mv.getCredits() != null ? mv.getCredits().toString() : null,
                    mv.getRequiredFeedbacks(), "creditsFeedback", "creditsAccepted");
            addFieldWithSpecificFeedback(document, "Total Hours", mv.getHoursTotal() != null ? mv.getHoursTotal().toString() : null,
                    mv.getRequiredFeedbacks(), "hoursTotalFeedback", "hoursTotalAccepted");
            addFieldWithSpecificFeedback(document, "Self-Study Hours", mv.getHoursSelfStudy() != null ? mv.getHoursSelfStudy().toString() : null,
                    mv.getRequiredFeedbacks(), "hoursSelfStudyFeedback", "hoursSelfStudyAccepted");
            addFieldWithSpecificFeedback(document, "Presence Hours", mv.getHoursPresence() != null ? mv.getHoursPresence().toString() : null,
                    mv.getRequiredFeedbacks(), "hoursPresenceFeedback", "hoursPresenceAccepted");

            // Content Sections
            document.add(new Paragraph("Content").setFontSize(16).setMarginTop(10));

            if (mv.getBulletPoints() != null && !mv.getBulletPoints().isBlank()) {
                addTextSection(document, mv.getBulletPoints());
            }

            addFieldWithSpecificFeedback(document, "Module Content", mv.getContentEng(), mv.getRequiredFeedbacks(), "contentFeedback", "contentAccepted");
            if (mv.getContentPromptEng() != null && !mv.getContentPromptEng().isBlank()) {
                addPromptSection(document, "Content Generation Prompt", mv.getContentPromptEng());
            }

            addFieldWithSpecificFeedback(document, "Learning Outcomes", mv.getLearningOutcomesEng(), mv.getRequiredFeedbacks(),
                    "learningOutcomesFeedback", "learningOutcomesAccepted");
            if (mv.getLearningOutcomesPromptEng() != null && !mv.getLearningOutcomesPromptEng().isBlank()) {
                addPromptSection(document, "Learning Outcomes Generation Prompt", mv.getLearningOutcomesPromptEng());
            }

            addFieldWithSpecificFeedback(document, "Teaching Methods", mv.getTeachingMethodsEng(), mv.getRequiredFeedbacks(),
                    "teachingMethodsFeedback", "teachingMethodsAccepted");
            if (mv.getTeachingMethodsPromptEng() != null && !mv.getTeachingMethodsPromptEng().isBlank()) {
                addPromptSection(document, "Teaching Methods Generation Prompt", mv.getTeachingMethodsPromptEng());
            }

            addFieldWithSpecificFeedback(document, "Examination Achievements", mv.getExaminationAchievementsEng(), mv.getRequiredFeedbacks(),
                    "examinationAchievementsFeedback", "examinationAchievementsAccepted");
            if (mv.getExaminationAchievementsPromptEng() != null && !mv.getExaminationAchievementsPromptEng().isBlank()) {
                addPromptSection(document, "Examination Achievements Generation Prompt", mv.getExaminationAchievementsPromptEng());
            }

            addFieldWithSpecificFeedback(document, "Recommended Prerequisites", mv.getRecommendedPrerequisitesEng(), mv.getRequiredFeedbacks(),
                    "recommendedPrerequisitesFeedback", "recommendedPrerequisitesAccepted");
            addFieldWithSpecificFeedback(document, "Media", mv.getMediaEng(), mv.getRequiredFeedbacks(), "mediaFeedback", "mediaAccepted");
            addFieldWithSpecificFeedback(document, "Literature", mv.getLiteratureEng(), mv.getRequiredFeedbacks(), "literatureFeedback", "literatureAccepted");

            // Responsibility Section
            document.add(new Paragraph("Responsibility").setFontSize(16).setMarginTop(10));

            addFieldWithSpecificFeedback(document, "Responsibles", mv.getResponsiblesEng(), mv.getRequiredFeedbacks(),
                    "responsiblesFeedback", "responsiblesAccepted");
            addFieldWithSpecificFeedback(document, "Lecturer", mv.getLvSwsLecturerEng(), mv.getRequiredFeedbacks(),
                    "lvSwsLecturerFeedback", "lvSwsLecturerAccepted");

            // Footer
            document.add(new Paragraph("Generated on " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(30));

            document.close();
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addFieldWithSpecificFeedback(Document document, String fieldName, String fieldValue, List<Feedback> feedbacks,
                                              String feedbackFieldName, String acceptedFieldName) {
        // Add field name and value
        document.add(new Paragraph(fieldName).setFontSize(12));
        document.add(new Paragraph(fieldValue != null ? fieldValue : "Not specified").setMarginBottom(5));

        // Add specific feedback if any
        if (feedbacks != null && !feedbacks.isEmpty()) {
            boolean hasFeedback = false;
            Table feedbackTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(10);

            for (Feedback feedback : feedbacks) {
                try {
                    // Get feedback text using reflection
                    java.lang.reflect.Field feedbackField = Feedback.class.getDeclaredField(feedbackFieldName);
                    feedbackField.setAccessible(true);
                    String feedbackText = (String) feedbackField.get(feedback);

                    // Get accepted status using reflection
                    java.lang.reflect.Field acceptedField = Feedback.class.getDeclaredField(acceptedFieldName);
                    acceptedField.setAccessible(true);
                    boolean isAccepted = (boolean) acceptedField.get(feedback);

                    if (feedbackText != null && !feedbackText.isBlank()) {
                        hasFeedback = true;
                        Cell roleCell = new Cell().add(new Paragraph(feedback.getRequiredRole().toString()));
                        Cell feedbackCell = new Cell().add(new Paragraph(feedbackText));

                        if (!isAccepted) {
                            roleCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                            feedbackCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                        } else {
                            roleCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                            feedbackCell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                        }

                        feedbackTable.addCell(roleCell);
                        feedbackTable.addCell(feedbackCell);
                    }
                } catch (Exception e) {
                    // Skip this feedback if there's an issue with reflection
                    continue;
                }
            }

            if (hasFeedback) {
                document.add(new Paragraph("Feedback:").setFontSize(10));
                document.add(feedbackTable);
            }
        }

        document.add(new Paragraph("").setMarginBottom(10));
    }

    private void addTextSection(Document document, String content) {
        document.add(new Paragraph("Key Points").setFontSize(12));
        document.add(new Paragraph(content != null ? content : "Not specified").setMarginBottom(10));
    }

    private void addPromptSection(Document document, String promptTitle, String promptContent) {
        document.add(new Paragraph(promptTitle).setFontSize(10));
        document.add(new Paragraph(promptContent).setFontSize(10).setMarginBottom(10));
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "Not specified")));
    }

    private void addContentSection(Document document, String title, String content) {
        document.add(new Paragraph(title).setFontSize(14).setMarginTop(20));
        document.add(new Paragraph(content != null ? content : "Not specified"));
    }
}
