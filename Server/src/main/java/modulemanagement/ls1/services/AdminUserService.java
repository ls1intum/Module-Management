package modulemanagement.ls1.services;

import modulemanagement.ls1.dtos.PageResponseDTO;
import modulemanagement.ls1.dtos.UpdateUserRoleDTO;
import modulemanagement.ls1.dtos.UserDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResponseDTO<UserDTO> getUsersPage(Pageable pageable, String search) {
        Page<User> page = search != null && !search.isBlank()
                ? userRepository.findBySearch(search.trim(), pageable)
                : userRepository.findAll(pageable);
        return PageResponseDTO.from(page.map(UserDTO::fromUser));
    }

    public UserDTO updateUserRole(UUID userId, UpdateUserRoleDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setRole(dto.getRole());
        user = userRepository.save(user);
        return UserDTO.fromUser(user);
    }
}
