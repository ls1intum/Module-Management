package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.AiReviewGuidelineDTO;
import modulemanagement.ls1.dtos.CreateAiReviewGuidelineDTO;
import modulemanagement.ls1.dtos.UpdateAiReviewGuidelineDTO;
import modulemanagement.ls1.models.AiReviewGuideline;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.AiReviewGuidelineRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiReviewGuidelineService {

    private final AiReviewGuidelineRepository aiReviewGuidelineRepository;

    public AiReviewGuidelineService(AiReviewGuidelineRepository aiReviewGuidelineRepository) {
        this.aiReviewGuidelineRepository = aiReviewGuidelineRepository;
    }

    public List<AiReviewGuidelineDTO> getAllGuidelines() {
        return aiReviewGuidelineRepository.findAllByOrderBySectionAscSortOrderAscGuidelineIdAsc().stream()
                .map(AiReviewGuidelineDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public AiReviewGuidelineDTO getGuideline(Long id) {
        AiReviewGuideline guideline = aiReviewGuidelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI review guideline not found: " + id));
        return AiReviewGuidelineDTO.fromEntity(guideline);
    }

    @Transactional
    public AiReviewGuidelineDTO createGuideline(User actor, CreateAiReviewGuidelineDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        AiReviewGuideline guideline = new AiReviewGuideline();
        guideline.setSection(dto.getSection());
        guideline.setTitle(dto.getTitle().trim());
        guideline.setInstruction(dto.getInstruction().trim());
        guideline.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        guideline.setCreatedBy(actor);
        guideline.setUpdatedBy(actor);
        guideline.setCreatedAt(now);
        guideline.setUpdatedAt(now);
        guideline = aiReviewGuidelineRepository.save(guideline);
        return AiReviewGuidelineDTO.fromEntity(guideline);
    }

    @Transactional
    public AiReviewGuidelineDTO updateGuideline(Long id, User actor, UpdateAiReviewGuidelineDTO dto) {
        AiReviewGuideline guideline = aiReviewGuidelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI review guideline not found: " + id));
        guideline.setSection(dto.getSection());
        guideline.setTitle(dto.getTitle().trim());
        guideline.setInstruction(dto.getInstruction().trim());
        guideline.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : guideline.getSortOrder());
        guideline.setUpdatedBy(actor);
        guideline.setUpdatedAt(LocalDateTime.now());
        guideline = aiReviewGuidelineRepository.save(guideline);
        return AiReviewGuidelineDTO.fromEntity(guideline);
    }

    @Transactional
    public void deleteGuideline(Long id) {
        AiReviewGuideline guideline = aiReviewGuidelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI review guideline not found: " + id));
        aiReviewGuidelineRepository.delete(guideline);
    }
}
