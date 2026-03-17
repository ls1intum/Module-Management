package modulemanagement.ls1.services;

import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.DegreeProgram;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.DegreeProgramRepository;
import modulemanagement.ls1.repositories.DegreeProgramSpecializationRepository;
import modulemanagement.ls1.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Keeps PROGRAM_COORDINATOR and SPECIALIZATION_AREA_RESPONSIBLE roles in sync:
 * - Program coordinators (responsible for a degree program) have PROGRAM_COORDINATOR.
 * - Specialization area responsibles have SPECIALIZATION_AREA_RESPONSIBLE.
 */
@Service
public class ResponsibleUserRoleService {

    private final UserRepository userRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final DegreeProgramSpecializationRepository degreeProgramSpecializationRepository;

    public ResponsibleUserRoleService(UserRepository userRepository,
            DegreeProgramRepository degreeProgramRepository,
            DegreeProgramSpecializationRepository degreeProgramSpecializationRepository) {
        this.userRepository = userRepository;
        this.degreeProgramRepository = degreeProgramRepository;
        this.degreeProgramSpecializationRepository = degreeProgramSpecializationRepository;
    }

    @Transactional
    public void ensureProgramCoordinatorRole(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        if (user.getRoles() != null && user.getRoles().contains(UserRole.PROGRAM_COORDINATOR))
            return;
        if (user.getRoles() == null) user.setRoles(new ArrayList<>());
        user.getRoles().add(UserRole.PROGRAM_COORDINATOR);
        userRepository.save(user);
    }

    @Transactional
    public void removeProgramCoordinatorRoleIfNotResponsible(UUID userId) {
        if (degreeProgramRepository.existsByResponsibleUser_UserId(userId))
            return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null) return;
        if (user.getRoles().remove(UserRole.PROGRAM_COORDINATOR))
            userRepository.save(user);
    }

    @Transactional
    public void ensureSpecializationAreaResponsibleRole(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        if (user.getRoles() != null && user.getRoles().contains(UserRole.SPECIALIZATION_AREA_RESPONSIBLE))
            return;
        if (user.getRoles() == null) user.setRoles(new ArrayList<>());
        user.getRoles().add(UserRole.SPECIALIZATION_AREA_RESPONSIBLE);
        userRepository.save(user);
    }

    @Transactional
    public void removeSpecializationAreaResponsibleRoleIfNotResponsible(UUID userId) {
        if (degreeProgramSpecializationRepository.existsByResponsibleUser_UserId(userId))
            return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null) return;
        if (user.getRoles().remove(UserRole.SPECIALIZATION_AREA_RESPONSIBLE))
            userRepository.save(user);
    }

    @Transactional
    public void unassignFromAllPrograms(UUID userId) {
        List<DegreeProgram> programs = degreeProgramRepository.findByResponsibleUser_UserId(userId);
        for (DegreeProgram p : programs) {
            p.setResponsibleUser(null);
        }
        if (!programs.isEmpty()) degreeProgramRepository.saveAll(programs);
    }

    @Transactional
    public void unassignFromAllSpecializations(UUID userId) {
        List<DegreeProgramSpecialization> specs = degreeProgramSpecializationRepository.findByResponsibleUser_UserId(userId);
        for (DegreeProgramSpecialization s : specs) {
            s.setResponsibleUser(null);
        }
        if (!specs.isEmpty()) degreeProgramSpecializationRepository.saveAll(specs);
    }
}
