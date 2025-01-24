package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.ModuleVersionUpdateRequestDTO;
import modulemanagement.ls1.dtos.ModuleVersionUpdateResponseDTO;
import modulemanagement.ls1.dtos.ModuleVersionViewDTO;
import modulemanagement.ls1.enums.FeedbackStatus;
import modulemanagement.ls1.enums.ModuleVersionStatus;
import modulemanagement.ls1.enums.ProposalStatus;
import modulemanagement.ls1.models.Feedback;
import modulemanagement.ls1.models.ModuleVersion;
import modulemanagement.ls1.models.Proposal;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.ModuleVersionRepository;
import modulemanagement.ls1.repositories.ProposalRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ModuleVersionService {
    private final ModuleVersionRepository moduleVersionRepository;
    private final ProposalRepository proposalRepository;

    public ModuleVersionService(ModuleVersionRepository moduleVersionRepository, ProposalRepository proposalRepository) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.proposalRepository = proposalRepository;
    }

    public ModuleVersionUpdateResponseDTO updateModuleVersionFromRequest(UUID userId, Long moduleVersionId, ModuleVersionUpdateRequestDTO request) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(() -> new ResourceNotFoundException("ModuleVersion not found"));
        if (!mv.getProposal().getCreatedBy().getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");

        if (!mv.getVersion().equals(mv.getProposal().getLatestModuleVersionWithContent().getVersion())) {
            throw new OptimisticLockingFailureException("Cannot update an outdated ModuleVersion");
        }

        if (!mv.getStatus().equals(ModuleVersionStatus.PENDING_SUBMISSION)) {
            throw new IllegalStateException("Cannot update a submitted ModuleVersion");
        }

        mv.setBulletPoints(request.getBulletPoints());
        mv.setTitleEng(request.getTitleEng());
        mv.setLevelEng(request.getLevelEng());
        mv.setLanguageEng(request.getLanguageEng());
        mv.setFrequencyEng(request.getFrequencyEng());
        mv.setCredits(request.getCredits());
        mv.setDuration(request.getDuration());
        mv.setHoursTotal(request.getHoursTotal());
        mv.setHoursSelfStudy(request.getHoursSelfStudy());
        mv.setHoursPresence(request.getHoursPresence());
        mv.setExaminationAchievementsEng(request.getExaminationAchievementsEng());
        mv.setExaminationAchievementsPromptEng(request.getExaminationAchievementsPromptEng());
        mv.setRepetitionEng(request.getRepetitionEng());
        mv.setRecommendedPrerequisitesEng(request.getRecommendedPrerequisitesEng());
        mv.setContentEng(request.getContentEng());
        mv.setContentPromptEng(request.getContentPromptEng());
        mv.setLearningOutcomesEng(request.getLearningOutcomesEng());
        mv.setLearningOutcomesPromptEng(request.getLearningOutcomesPromptEng());
        mv.setTeachingMethodsEng(request.getTeachingMethodsEng());
        mv.setTeachingMethodsPromptEng(request.getTeachingMethodsPromptEng());
        mv.setMediaEng(request.getMediaEng());
        mv.setMediaPromptEng(request.getMediaPromptEng());
        mv.setLiteratureEng(request.getLiteratureEng());
        mv.setResponsiblesEng(request.getResponsiblesEng());
        mv.setLvSwsLecturerEng(request.getLvSwsLecturerEng());

        mv = moduleVersionRepository.save(mv);
        return ModuleVersionUpdateResponseDTO.fromModuleVersion(mv);
    }

    public void updateStatus(Long moduleVersionId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).
                orElseThrow(() -> new ResourceNotFoundException("Could not update corresponding module version status"));
        if (mv.getStatus().equals(ModuleVersionStatus.CANCELLED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Proposal was cancelled by the submitter.");
        }
        Proposal p = mv.getProposal();
        if (!mv.equals(p.getLatestModuleVersionWithContent())) {
            return;
        }

        boolean allFeedbackPositive = true;
        boolean oneFeedbackNegative = false;
        for (Feedback feedback: mv.getRequiredFeedbacks()) {
            if (!feedback.getStatus().equals(FeedbackStatus.APPROVED)) {
                allFeedbackPositive = false;
            }
            if (feedback.getStatus().equals(FeedbackStatus.REJECTED)) {
                oneFeedbackNegative = true;
            }
        }

        if (allFeedbackPositive) {
            mv.setStatus(ModuleVersionStatus.ACCEPTED);
            p.setStatus(ProposalStatus.ACCEPTED);
            mv.getProposal().setStatus(ProposalStatus.ACCEPTED);
        }
        if (oneFeedbackNegative) {
            mv.setStatus(ModuleVersionStatus.REJECTED);
            p.setStatus(ProposalStatus.REQUIRES_REVIEW);
        }
        proposalRepository.save(p);
        moduleVersionRepository.save(mv);
    }

    public ModuleVersionUpdateResponseDTO getModuleVersionUpdateDtoFromId(Long moduleVersionId, UUID userId) {
        var mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(() -> new ResourceNotFoundException("Module Version not found"));
        Proposal p = mv.getProposal();
        if (!p.getCreatedBy().getUserId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        if (p.getModuleVersions() == null || p.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        return ModuleVersionUpdateResponseDTO.fromModuleVersion(mv);
    }

    public ModuleVersionViewDTO getModuleVersionViewDto(Long moduleVersionId, UUID userId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).
                orElseThrow(() -> new ResourceNotFoundException("Could not find a module version with this ID."));
        Proposal p = mv.getProposal();
        if (!p.getCreatedBy().getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access.");
        return ModuleVersionViewDTO.from(mv);
    }
}
