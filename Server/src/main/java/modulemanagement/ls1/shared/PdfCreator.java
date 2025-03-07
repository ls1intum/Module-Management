package modulemanagement.ls1.shared;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import modulemanagement.ls1.models.ModuleVersion;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

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

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "Not specified")));
    }

    private void addContentSection(Document document, String title, String content) {
        document.add(new Paragraph(title).setFontSize(14).setMarginTop(20));
        document.add(new Paragraph(content != null ? content : "Not specified"));
    }
}
