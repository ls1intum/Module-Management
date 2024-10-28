package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.dtos.ModuleVersionUpdateRequestDTO;
import de.tum.in.www1.modulemanagement.enums.FeedbackStatus;
import de.tum.in.www1.modulemanagement.models.Feedback;
import de.tum.in.www1.modulemanagement.models.ModuleVersion;
import de.tum.in.www1.modulemanagement.repositories.ModuleVersionRepository;
import de.tum.in.www1.modulemanagement.shared.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleVersionService {
    private final ModuleVersionRepository moduleVersionRepository;

    public ModuleVersionService(ModuleVersionRepository moduleVersionRepository) {
        this.moduleVersionRepository = moduleVersionRepository;
    }

    public ModuleVersion createNewModuleVersion() {
        return null;
    }

    public ModuleVersion updateModuleVersionFromRequest(Long moduleVersionId, ModuleVersionUpdateRequestDTO request) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(() -> new ResourceNotFoundException("ModuleVersion not found"));
        if (!mv.getProposal().getCreatedBy().getUserId().equals(request.getUserId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");

        if (!mv.getVersion().equals(mv.getProposal().getLatestModuleVersion().getVersion())) {
            throw new OptimisticLockingFailureException("Cannot update an outdated ModuleVersion");
        }

        if (!mv.getStatus().equals("PENDING_SUBMISSION")) {
            throw new IllegalStateException("Cannot update a submitted ModuleVersion");
        }

        mv.setTitleEng(request.getTitleEng());
        mv.setLevelEng(request.getLevelEng());
        mv.setLanguageEng(request.getLanguageEng());
        mv.setFrequencyEng(request.getFrequencyEng());
        mv.setCredits(request.getCredits());
        mv.setHoursTotal(request.getHoursTotal());
        mv.setHoursSelfStudy(request.getHoursSelfStudy());
        mv.setHoursPresence(request.getHoursPresence());
        mv.setExaminationAchievementsEng(request.getExaminationAchievementsEng());
        mv.setRepetitionEng(request.getRepetitionEng());
        mv.setRecommendedPrerequisitesEng(request.getRecommendedPrerequisitesEng());
        mv.setContentEng(request.getContentEng());
        mv.setLearningOutcomesEng(request.getLearningOutcomesEng());
        mv.setTeachingMethodsEng(request.getTeachingMethodsEng());
        mv.setMediaEng(request.getMediaEng());
        mv.setLiteratureEng(request.getLiteratureEng());
        mv.setResponsiblesEng(request.getResponsiblesEng());
        mv.setLvSwsLecturerEng(request.getLvSwsLecturerEng());

        mv = moduleVersionRepository.save(mv);
        return mv;
    }

    public void updateStatus(Long moduleVersionId) {
        ModuleVersion mv = moduleVersionRepository.findById(moduleVersionId).orElseThrow(() -> new ResourceNotFoundException("Could not update corresponding module version status"));
        boolean isModuleVersionApproved = true;
        boolean isModuleVersionRejected = false;
        for (Feedback feedback: mv.getRequiredFeedbacks()) {
            if (!feedback.getStatus().equals(FeedbackStatus.APPROVED)) {
                isModuleVersionApproved = false;
            }
            if (feedback.getStatus().equals(FeedbackStatus.REJECTED)) {
                isModuleVersionRejected = true;
            }
        }
        if (isModuleVersionApproved) {
            mv.setStatus("APPROVED");
        }
        if (isModuleVersionRejected) {
            mv.setStatus("REJECTED");
        }
        moduleVersionRepository.save(mv);
    }
}
