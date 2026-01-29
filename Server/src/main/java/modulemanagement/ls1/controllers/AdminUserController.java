package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.PageResponseDTO;
import modulemanagement.ls1.dtos.UpdateUserRoleDTO;
import modulemanagement.ls1.dtos.UserDTO;
import modulemanagement.ls1.services.AdminUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getUsersPage(pageable, search));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleDTO dto) {
        return ResponseEntity.ok(adminUserService.updateUserRole(userId, dto));
    }
}
