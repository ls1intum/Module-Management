package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.dtos.ModuleVersionUpdateRequestDTO;
import de.tum.in.www1.modulemanagement.enums.FeedbackStatus;
import de.tum.in.www1.modulemanagement.enums.ModuleVersionStatus;
import de.tum.in.www1.modulemanagement.enums.ProposalStatus;
import de.tum.in.www1.modulemanagement.models.Feedback;
import de.tum.in.www1.modulemanagement.models.ModuleVersion;
import de.tum.in.www1.modulemanagement.models.Proposal;
import de.tum.in.www1.modulemanagement.repositories.ModuleVersionRepository;
import de.tum.in.www1.modulemanagement.repositories.ProposalRepository;
import de.tum.in.www1.modulemanagement.shared.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleVersionService {
    private final ModuleVersionRepository moduleVersionRepository;
    private final ProposalRepository proposalRepository;

    public ModuleVersionService(ModuleVersionRepository moduleVersionRepository, ProposalRepository proposalRepository) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.proposalRepository = proposalRepository;
    }

    public ModuleVersionUpdateRequestDTO updateModuleVersionFromRequest(Long moduleVersionId, ModuleVersionUpdateRequestDTO request) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(() -> new ResourceNotFoundException("ModuleVersion not found"));
        if (!mv.getProposal().getCreatedBy().getUserId().equals(request.getUserId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");

        if (!mv.getVersion().equals(mv.getProposal().getLatestModuleVersionWithContent().getVersion())) {
            throw new OptimisticLockingFailureException("Cannot update an outdated ModuleVersion");
        }

        if (!mv.getStatus().equals(ModuleVersionStatus.PENDING_SUBMISSION)) {
            throw new IllegalStateException("Cannot update a submitted ModuleVersion");
        }

        ModuleVersionUpdateRequestDTO mdto = new ModuleVersionUpdateRequestDTO();

        mdto.setUserId(mv.getProposal().getCreatedBy().getUserId());
        mdto.setModuleVersionId(mv.getModuleVersionId());
        mdto.setVersion(mv.getVersion());
        mdto.setTitleEng(request.getTitleEng());
        mdto.setLevelEng(request.getLevelEng());
        mdto.setLanguageEng(request.getLanguageEng());
        mdto.setFrequencyEng(request.getFrequencyEng());
        mdto.setCredits(request.getCredits());
        mdto.setHoursTotal(request.getHoursTotal());
        mdto.setHoursSelfStudy(request.getHoursSelfStudy());
        mdto.setHoursPresence(request.getHoursPresence());
        mdto.setExaminationAchievementsEng(request.getExaminationAchievementsEng());
        mdto.setRepetitionEng(request.getRepetitionEng());
        mdto.setRecommendedPrerequisitesEng(request.getRecommendedPrerequisitesEng());
        mdto.setContentEng(request.getContentEng());
        mdto.setLearningOutcomesEng(request.getLearningOutcomesEng());
        mdto.setTeachingMethodsEng(request.getTeachingMethodsEng());
        mdto.setMediaEng(request.getMediaEng());
        mdto.setLiteratureEng(request.getLiteratureEng());
        mdto.setResponsiblesEng(request.getResponsiblesEng());
        mdto.setLvSwsLecturerEng(request.getLvSwsLecturerEng());

        mv = moduleVersionRepository.save(mv);
        return mv;
    }

    public void updateStatus(Long moduleVersionId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).
                orElseThrow(() -> new ResourceNotFoundException("Could not update corresponding module version status"));

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
        Proposal p = mv.getProposal();
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

    public ModuleVersionUpdateRequestDTO getModuleVersionUpdateDtoFromId(Long moduleVersionId, Long userId) {
        var mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(() -> new ResourceNotFoundException("Module Version not found"));
        Proposal p = mv.getProposal();
        if (!p.getCreatedBy().getUserId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
        }

        if (p.getModuleVersions() == null || p.getModuleVersions().isEmpty()) {
            throw new IllegalStateException("Proposal must have at least one ModuleVersion.");
        }

        if (!mv.getStatus().equals(ModuleVersionStatus.PENDING_SUBMISSION) && !mv.getStatus().equals(ModuleVersionStatus.PENDING_FEEDBACK)) {
            throw new IllegalStateException("Only proposals pending submission or feedback can be updated.");
        }

        ModuleVersionUpdateRequestDTO dto = new ModuleVersionUpdateRequestDTO();
        dto.setUserId(userId);
        dto.setModuleVersionId(mv.getModuleVersionId());
        dto.setVersion(mv.getVersion());
        dto.setModuleId(mv.getModuleId());
        dto.setStatus(mv.getStatus());
        dto.setIsComplete(mv.isCompleted());
        dto.setTitleEng(mv.getTitleEng());
        dto.setLevelEng(mv.getLevelEng());
        dto.setLanguageEng(mv.getLanguageEng());
        dto.setFrequencyEng(mv.getFrequencyEng());
        dto.setCredits(mv.getCredits());
        dto.setHoursTotal(mv.getHoursTotal());
        dto.setHoursSelfStudy(mv.getHoursSelfStudy());
        dto.setHoursPresence(mv.getHoursPresence());
        dto.setExaminationAchievementsEng(mv.getExaminationAchievementsEng());
        dto.setRepetitionEng(mv.getRepetitionEng());
        dto.setRecommendedPrerequisitesEng(mv.getRecommendedPrerequisitesEng());
        dto.setContentEng(mv.getContentEng());
        dto.setLearningOutcomesEng(mv.getLearningOutcomesEng());
        dto.setTeachingMethodsEng(mv.getTeachingMethodsEng());
        dto.setMediaEng(mv.getMediaEng());
        dto.setLiteratureEng(mv.getLiteratureEng());
        dto.setResponsiblesEng(mv.getResponsiblesEng());
        dto.setLvSwsLecturerEng(mv.getLvSwsLecturerEng());
        return dto;
    }
}
