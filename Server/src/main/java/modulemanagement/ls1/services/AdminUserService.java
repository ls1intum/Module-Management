package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.PageResponseDTO;
import modulemanagement.ls1.dtos.UpdateUserRoleDTO;
import modulemanagement.ls1.dtos.UserDTO;
import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final ResponsibleUserRoleService responsibleUserRoleService;

    public AdminUserService(UserRepository userRepository, ResponsibleUserRoleService responsibleUserRoleService) {
        this.userRepository = userRepository;
        this.responsibleUserRoleService = responsibleUserRoleService;
    }

    public PageResponseDTO<UserDTO> getUsersPage(Pageable pageable, String search) {
        Page<User> page = search != null && !search.isBlank()
                ? userRepository.findBySearch(search.trim(), pageable)
                : userRepository.findAll(pageable);
        return PageResponseDTO.from(page.map(UserDTO::fromUser));
    }

    @Transactional
    public UserDTO updateUserRole(UUID userId, UpdateUserRoleDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        List<UserRole> currentRoles = user.getRoles() != null ? user.getRoles() : List.of();
        List<UserRole> newRoles = dto.getRoles() != null ? dto.getRoles() : List.of();
        if (currentRoles.contains(UserRole.PROGRAM_COORDINATOR) && !newRoles.contains(UserRole.PROGRAM_COORDINATOR)) {
            responsibleUserRoleService.unassignFromAllPrograms(userId);
        }
        if (currentRoles.contains(UserRole.SPECIALIZATION_AREA_RESPONSIBLE) && !newRoles.contains(UserRole.SPECIALIZATION_AREA_RESPONSIBLE)) {
            responsibleUserRoleService.unassignFromAllSpecializations(userId);
        }
        user.setRoles(dto.getRoles());
        user = userRepository.save(user);
        return UserDTO.fromUser(user);
    }
}
