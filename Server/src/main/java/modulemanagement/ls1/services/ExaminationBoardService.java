package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.*;
import modulemanagement.ls1.models.ExaminationBoard;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.DegreeProgramRepository;
import modulemanagement.ls1.repositories.ExaminationBoardRepository;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExaminationBoardService {

    private final ExaminationBoardRepository examinationBoardRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final UserRepository userRepository;
    private final UserRolesSyncService userRolesSyncService;

    public ExaminationBoardService(ExaminationBoardRepository examinationBoardRepository,
            DegreeProgramRepository degreeProgramRepository,
            UserRepository userRepository,
            UserRolesSyncService userRolesSyncService) {
        this.examinationBoardRepository = examinationBoardRepository;
        this.degreeProgramRepository = degreeProgramRepository;
        this.userRepository = userRepository;
        this.userRolesSyncService = userRolesSyncService;
    }

    public List<ExaminationBoardSummaryDTO> getAllExaminationBoards() {
        return examinationBoardRepository.findAll().stream()
                .map(ExaminationBoardSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ExaminationBoardDTO getExaminationBoard(Long id) {
        ExaminationBoard board = examinationBoardRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination board not found: " + id));
        return ExaminationBoardDTO.fromEntity(board);
    }

    @Transactional
    public ExaminationBoardDTO createExaminationBoard(CreateExaminationBoardDTO dto) {
        ExaminationBoard board = new ExaminationBoard();
        board.setName(dto.getName().trim());
        board = examinationBoardRepository.save(board);
        return ExaminationBoardDTO.fromEntity(
                examinationBoardRepository.findByIdWithMembers(board.getExaminationBoardId()).orElse(board));
    }

    @Transactional
    public ExaminationBoardDTO updateExaminationBoard(Long id, UpdateExaminationBoardDTO dto) {
        ExaminationBoard board = examinationBoardRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination board not found: " + id));

        board.setName(dto.getName().trim());

        Set<UUID> previousMemberIds = board.getMembers().stream().map(User::getUserId).collect(Collectors.toSet());
        Set<UUID> newMemberIds = new HashSet<>(dto.getUserIds());

        board.getMembers().clear();
        for (UUID userId : newMemberIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            board.getMembers().add(user);
            userRolesSyncService.ensureExaminationBoardRole(userId);
        }
        examinationBoardRepository.save(board);

        for (UUID oldId : previousMemberIds) {
            if (!newMemberIds.contains(oldId)) {
                userRolesSyncService.removeExaminationBoardRoleIfNotMember(oldId);
            }
        }

        return ExaminationBoardDTO.fromEntity(
                examinationBoardRepository.findByIdWithMembers(id).orElse(board));
    }

    @Transactional
    public void deleteExaminationBoard(Long id) {
        ExaminationBoard board = examinationBoardRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Examination board not found: " + id));
        java.util.Set<UUID> memberIds = board.getMembers().stream().map(User::getUserId).collect(Collectors.toSet());
        degreeProgramRepository.clearExaminationBoardIdByExaminationBoardId(id);
        examinationBoardRepository.delete(board);
        for (UUID uid : memberIds) {
            userRolesSyncService.removeExaminationBoardRoleIfNotMember(uid);
        }
    }
}
