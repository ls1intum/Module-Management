package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.models.CitModule;
import de.tum.in.www1.modulemanagement.models.ModuleTranslation;
import de.tum.in.www1.modulemanagement.repositories.ModuleRepository;
import de.tum.in.www1.modulemanagement.shared.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public CitModule createModule(CitModule module) {
        if (module.getTranslations() != null) {
            for (ModuleTranslation translation : module.getTranslations()) {
                translation.setCitModule(module);
            }
        }
        return moduleRepository.save(module);
    }

    public CitModule findById(long id) {
        return moduleRepository.findById(id).orElse(null);
    }

    public CitModule findByModuleId(String moduleId) {
        return moduleRepository.findByModuleId(moduleId).orElse(null);
    }

    public List<CitModule> getAllModules() {
        return moduleRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(CitModule::getId))
                .collect(Collectors.toList());
    }

    public CitModule updateModule(Long id, CitModule updatedModule) {
        CitModule existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id " + id));

        existingModule.setModuleId(updatedModule.getModuleId());
        existingModule.setCreationDate(updatedModule.getCreationDate());

        if (updatedModule.getTranslations() != null) {
            for (ModuleTranslation updatedTranslation : updatedModule.getTranslations()) {
                ModuleTranslation existingTranslation = null;
                for (ModuleTranslation translation : existingModule.getTranslations()) {
                    if (translation.getLanguage().equals(updatedTranslation.getLanguage())) {
                        existingTranslation = translation;
                        break;
                    }
                }

                if (existingTranslation != null) {
                    existingTranslation.setTitle(updatedTranslation.getTitle());
                    existingTranslation.setRecommendedPrerequisites(updatedTranslation.getRecommendedPrerequisites());
                    existingTranslation.setAssessmentMethod(updatedTranslation.getAssessmentMethod());
                    existingTranslation.setLearningOutcomes(updatedTranslation.getLearningOutcomes());
                    existingTranslation.setMediaUsed(updatedTranslation.getMediaUsed());
                    existingTranslation.setLiterature(updatedTranslation.getLiterature());
                } else {
                    updatedTranslation.setCitModule(existingModule);
                    existingModule.getTranslations().add(updatedTranslation);
                }
            }
        }

        return moduleRepository.save(existingModule);
    }

    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }

}
