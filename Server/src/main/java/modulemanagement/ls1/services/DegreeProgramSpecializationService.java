package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.repositories.DegreeProgramSpecializationRepository;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DegreeProgramSpecializationService {

    private final DegreeProgramSpecializationRepository degreeProgramSpecializationRepository;
    private final UserRepository userRepository;
    private final ResponsibleUserRoleService responsibleUserRoleService;

    public DegreeProgramSpecializationService(
            DegreeProgramSpecializationRepository degreeProgramSpecializationRepository,
            UserRepository userRepository,
            ResponsibleUserRoleService responsibleUserRoleService) {
        this.degreeProgramSpecializationRepository = degreeProgramSpecializationRepository;
        this.userRepository = userRepository;
        this.responsibleUserRoleService = responsibleUserRoleService;
    }

    public List<DegreeProgramSpecializationDTO> getAllDegreeProgramSpecializations() {
        return degreeProgramSpecializationRepository.findAllWithResponsibleUser().stream()
                .map(DegreeProgramSpecializationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public DegreeProgramSpecializationDTO createDegreeProgramSpecialization(CreateDegreeProgramSpecializationDTO dto) {
        DegreeProgramSpecialization entity = new DegreeProgramSpecialization();
        entity.setName(dto.getName());
        entity.setResponsibleUser(userRepository.getReferenceById(dto.getResponsibleUserId()));
        entity = degreeProgramSpecializationRepository.save(entity);
        responsibleUserRoleService.ensureSpecializationAreaCoordinatorRole(dto.getResponsibleUserId());
        return DegreeProgramSpecializationDTO.fromEntity(
                degreeProgramSpecializationRepository
                        .findByIdWithResponsibleUser(entity.getDegreeProgramSpecializationId()).orElse(entity));
    }

    public DegreeProgramSpecializationDTO updateDegreeProgramSpecialization(Long id,
            UpdateDegreeProgramSpecializationDTO dto) {
        DegreeProgramSpecialization entity = degreeProgramSpecializationRepository.findByIdWithResponsibleUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Degree program specialization not found: " + id));
        if (dto.getName() != null)
            entity.setName(dto.getName());
        if (dto.getResponsibleUserId() != null) {
            UUID previousUserId = entity.getResponsibleUser() != null ? entity.getResponsibleUser().getUserId() : null;
            entity.setResponsibleUser(userRepository.getReferenceById(dto.getResponsibleUserId()));
            entity = degreeProgramSpecializationRepository.save(entity);
            responsibleUserRoleService.ensureSpecializationAreaCoordinatorRole(dto.getResponsibleUserId());
            if (previousUserId != null && !previousUserId.equals(dto.getResponsibleUserId()))
                responsibleUserRoleService.removeSpecializationAreaCoordinatorRoleIfNotResponsible(previousUserId);
        } else {
            entity = degreeProgramSpecializationRepository.save(entity);
        }
        return DegreeProgramSpecializationDTO.fromEntity(entity);
    }

    public void deleteDegreeProgramSpecialization(Long id) {
        DegreeProgramSpecialization entity = degreeProgramSpecializationRepository.findByIdWithResponsibleUser(id).orElse(null);
        if (entity == null) {
            throw new ResourceNotFoundException("Degree program specialization not found: " + id);
        }
        UUID responsibleUserId = entity.getResponsibleUser() != null ? entity.getResponsibleUser().getUserId() : null;
        degreeProgramSpecializationRepository.deleteById(id);
        if (responsibleUserId != null)
            responsibleUserRoleService.removeSpecializationAreaCoordinatorRoleIfNotResponsible(responsibleUserId);
    }
}
