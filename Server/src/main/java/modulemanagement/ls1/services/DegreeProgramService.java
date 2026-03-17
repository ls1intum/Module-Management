package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.DegreeProgram;
import modulemanagement.ls1.repositories.DegreeProgramRepository;
import modulemanagement.ls1.repositories.DegreeProgramSpecializationRepository;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DegreeProgramService {

    private final DegreeProgramRepository degreeProgramRepository;
    private final DegreeProgramSpecializationRepository degreeProgramSpecializationRepository;
    private final UserRepository userRepository;
    private final ResponsibleUserRoleService responsibleUserRoleService;

    public DegreeProgramService(DegreeProgramRepository degreeProgramRepository,
            DegreeProgramSpecializationRepository degreeProgramSpecializationRepository,
            UserRepository userRepository,
            ResponsibleUserRoleService responsibleUserRoleService) {
        this.degreeProgramRepository = degreeProgramRepository;
        this.degreeProgramSpecializationRepository = degreeProgramSpecializationRepository;
        this.userRepository = userRepository;
        this.responsibleUserRoleService = responsibleUserRoleService;
    }

    public List<DegreeProgramDTO> getAllDegreePrograms() {
        return degreeProgramRepository.findAllWithResponsibleUser().stream()
                .map(DegreeProgramDTO::fromDegreeProgram)
                .collect(Collectors.toList());
    }

    public List<DegreeProgramDTO> getAllDegreeProgramsWithSpecializations() {
        return degreeProgramRepository.findAllWithSpecializations().stream()
                .map(DegreeProgramDTO::fromDegreeProgram)
                .collect(Collectors.toList());
    }

    public DegreeProgramDTO getDegreeProgram(Long id) {
        DegreeProgram program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Degree program not found: " + id));
        return DegreeProgramDTO.fromDegreeProgram(program);
    }

    public DegreeProgramDTO createDegreeProgram(CreateDegreeProgramDTO dto) {
        DegreeProgram program = new DegreeProgram();
        program.setName(dto.getName());
        program.setResponsibleUser(userRepository.getReferenceById(dto.getResponsibleUserId()));
        program = degreeProgramRepository.save(program);
        responsibleUserRoleService.ensureProgramCoordinatorRole(dto.getResponsibleUserId());
        program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(program.getDegreeProgramId())
                .orElse(program);
        return DegreeProgramDTO.fromDegreeProgram(program);
    }

    public DegreeProgramDTO updateDegreeProgram(Long id, UpdateDegreeProgramDTO dto) {
        DegreeProgram program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Degree program not found: " + id));
        if (dto.getName() != null)
            program.setName(dto.getName());
        if (dto.getResponsibleUserId() != null) {
            UUID previousUserId = program.getResponsibleUser() != null ? program.getResponsibleUser().getUserId() : null;
            program.setResponsibleUser(userRepository.getReferenceById(dto.getResponsibleUserId()));
            program = degreeProgramRepository.save(program);
            responsibleUserRoleService.ensureProgramCoordinatorRole(dto.getResponsibleUserId());
            if (previousUserId != null && !previousUserId.equals(dto.getResponsibleUserId()))
                responsibleUserRoleService.removeProgramCoordinatorRoleIfNotResponsible(previousUserId);
        } else {
            program = degreeProgramRepository.save(program);
        }
        program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(id).orElse(program);
        return DegreeProgramDTO.fromDegreeProgram(program);
    }

    public void deleteDegreeProgram(Long id) {
        DegreeProgram program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(id).orElse(null);
        if (program == null) {
            throw new ResourceNotFoundException("Degree program not found: " + id);
        }
        UUID responsibleUserId = program.getResponsibleUser() != null ? program.getResponsibleUser().getUserId() : null;
        degreeProgramRepository.deleteById(id);
        if (responsibleUserId != null)
            responsibleUserRoleService.removeProgramCoordinatorRoleIfNotResponsible(responsibleUserId);
    }

    public DegreeProgramDTO addSpecializationsToDegreeProgram(Long degreeProgramId,
            List<Long> degreeProgramSpecializationIds) {

        DegreeProgram program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(degreeProgramId)
                .orElseThrow(() -> new ResourceNotFoundException("Degree program not found: " + degreeProgramId));

        if (degreeProgramSpecializationIds == null || degreeProgramSpecializationIds.isEmpty()) {
            return DegreeProgramDTO.fromDegreeProgram(program);
        }

        var existingIds = program.getDegreeProgramSpecializations().stream()
                .map(DegreeProgramSpecialization::getDegreeProgramSpecializationId)
                .collect(Collectors.toSet());

        for (Long specId : degreeProgramSpecializationIds) {
            if (existingIds.contains(specId))
                continue;
            program.getDegreeProgramSpecializations()
                    .add(degreeProgramSpecializationRepository.getReferenceById(specId));
            existingIds.add(specId);
        }
        program = degreeProgramRepository.save(program);
        return DegreeProgramDTO.fromDegreeProgram(
                degreeProgramRepository.findWithSpecializationsByDegreeProgramId(degreeProgramId).orElse(program));
    }

    public DegreeProgramDTO removeSpecializationFromDegreeProgram(Long degreeProgramId,
            Long degreeProgramSpecializationId) {
        DegreeProgram program = degreeProgramRepository.findWithSpecializationsByDegreeProgramId(degreeProgramId)
                .orElseThrow(() -> new ResourceNotFoundException("Degree program not found: " + degreeProgramId));
        program.getDegreeProgramSpecializations()
                .removeIf(s -> s.getDegreeProgramSpecializationId().equals(degreeProgramSpecializationId));
        program = degreeProgramRepository.save(program);
        return DegreeProgramDTO.fromDegreeProgram(program);
    }
}
