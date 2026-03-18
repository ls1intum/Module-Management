package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.repositories.DegreeProgramSpecializationRepository;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DegreeProgramSpecializationService {

    private final DegreeProgramSpecializationRepository degreeProgramSpecializationRepository;
    private final UserRepository userRepository;

    public DegreeProgramSpecializationService(
            DegreeProgramSpecializationRepository degreeProgramSpecializationRepository,
            UserRepository userRepository) {
        this.degreeProgramSpecializationRepository = degreeProgramSpecializationRepository;
        this.userRepository = userRepository;
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
        if (dto.getResponsibleUserId() != null)
            entity.setResponsibleUser(userRepository.getReferenceById(dto.getResponsibleUserId()));
        entity = degreeProgramSpecializationRepository.save(entity);
        return DegreeProgramSpecializationDTO.fromEntity(entity);
    }

    public void deleteDegreeProgramSpecialization(Long id) {
        if (!degreeProgramSpecializationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Degree program specialization not found: " + id);
        }
        degreeProgramSpecializationRepository.deleteById(id);
    }
}
