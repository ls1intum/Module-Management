package modulemanagement.ls1.services;

import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.DegreeProgram;
import modulemanagement.ls1.models.DegreeProgramSpecialization;
import modulemanagement.ls1.models.ExaminationBoard;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.DegreeProgramRepository;
import modulemanagement.ls1.repositories.DegreeProgramSpecializationRepository;
import modulemanagement.ls1.repositories.ExaminationBoardRepository;
import modulemanagement.ls1.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Keeps PROGRAM_COORDINATOR and SPECIALIZATION_AREA_COORDINATOR roles in sync:
 * - Program coordinators (responsible for a degree program) have
 * PROGRAM_COORDINATOR.
 * - Specialization area coordinators have SPECIALIZATION_AREA_COORDINATOR.
 */
@Service
public class UserRolesSyncService {

    private final UserRepository userRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final DegreeProgramSpecializationRepository degreeProgramSpecializationRepository;
    private final ExaminationBoardRepository examinationBoardRepository;

    public UserRolesSyncService(UserRepository userRepository,
            DegreeProgramRepository degreeProgramRepository,
            DegreeProgramSpecializationRepository degreeProgramSpecializationRepository,
            ExaminationBoardRepository examinationBoardRepository) {
        this.userRepository = userRepository;
        this.degreeProgramRepository = degreeProgramRepository;
        this.degreeProgramSpecializationRepository = degreeProgramSpecializationRepository;
        this.examinationBoardRepository = examinationBoardRepository;
    }

    @Transactional
    public void ensureProgramCoordinatorRole(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return;
        if (user.getRoles() != null && user.getRoles().contains(UserRole.PROGRAM_COORDINATOR))
            return;
        if (user.getRoles() == null)
            user.setRoles(new ArrayList<>());
        user.getRoles().add(UserRole.PROGRAM_COORDINATOR);
        userRepository.save(user);
    }

    @Transactional
    public void removeProgramCoordinatorRoleIfNotResponsible(UUID userId) {
        if (degreeProgramRepository.existsByResponsibleUser_UserId(userId))
            return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null)
            return;
        if (user.getRoles().remove(UserRole.PROGRAM_COORDINATOR))
            userRepository.save(user);
    }

    @Transactional
    public void ensureSpecializationAreaCoordinatorRole(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return;
        if (user.getRoles() != null && user.getRoles().contains(UserRole.SPECIALIZATION_AREA_COORDINATOR))
            return;
        if (user.getRoles() == null)
            user.setRoles(new ArrayList<>());
        user.getRoles().add(UserRole.SPECIALIZATION_AREA_COORDINATOR);
        userRepository.save(user);
    }

    @Transactional
    public void removeSpecializationAreaCoordinatorRoleIfNotResponsible(UUID userId) {
        if (degreeProgramSpecializationRepository.existsByResponsibleUser_UserId(userId))
            return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null)
            return;
        if (user.getRoles().remove(UserRole.SPECIALIZATION_AREA_COORDINATOR))
            userRepository.save(user);
    }

    @Transactional
    public void unassignFromAllPrograms(UUID userId) {
        List<DegreeProgram> programs = degreeProgramRepository.findByResponsibleUser_UserId(userId);
        for (DegreeProgram p : programs) {
            p.setResponsibleUser(null);
        }
        if (!programs.isEmpty())
            degreeProgramRepository.saveAll(programs);
    }

    @Transactional
    public void unassignFromAllSpecializations(UUID userId) {
        List<DegreeProgramSpecialization> specs = degreeProgramSpecializationRepository
                .findByResponsibleUser_UserId(userId);
        for (DegreeProgramSpecialization s : specs) {
            s.setResponsibleUser(null);
        }
        if (!specs.isEmpty())
            degreeProgramSpecializationRepository.saveAll(specs);
    }

    @Transactional
    public void ensureExaminationBoardRole(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return;
        if (user.getRoles() != null && user.getRoles().contains(UserRole.EXAMINATION_BOARD))
            return;
        if (user.getRoles() == null)
            user.setRoles(new ArrayList<>());
        user.getRoles().add(UserRole.EXAMINATION_BOARD);
        userRepository.save(user);
    }

    @Transactional
    public void removeExaminationBoardRoleIfNotMember(UUID userId) {
        if (examinationBoardRepository.existsByMemberUserId(userId))
            return;
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null)
            return;
        if (user.getRoles().remove(UserRole.EXAMINATION_BOARD))
            userRepository.save(user);
    }

    @Transactional
    public void unassignFromAllExaminationBoards(UUID userId) {
        List<ExaminationBoard> boards = examinationBoardRepository.findByMemberUserId(userId);
        for (ExaminationBoard b : boards) {
            b.getMembers().removeIf(u -> u.getUserId().equals(userId));
        }
        if (!boards.isEmpty())
            examinationBoardRepository.saveAll(boards);
    }
}
