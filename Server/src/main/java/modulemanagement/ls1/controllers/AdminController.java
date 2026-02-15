package modulemanagement.ls1.controllers;

import jakarta.validation.Valid;
import modulemanagement.ls1.dtos.CreateSchoolDTO;
import modulemanagement.ls1.dtos.PageResponseDTO;
import modulemanagement.ls1.dtos.UpdateUserRoleDTO;
import modulemanagement.ls1.dtos.UserDTO;
import modulemanagement.ls1.models.School;
import modulemanagement.ls1.services.UserService;
import modulemanagement.ls1.services.SchoolService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService adminUserService;
    private final SchoolService schoolService;

    public AdminController(UserService adminUserService, SchoolService schoolService) {
        this.adminUserService = adminUserService;
        this.schoolService = schoolService;
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponseDTO<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getUsersPage(pageable, search));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleDTO dto) {
        return ResponseEntity.ok(adminUserService.updateUserRole(userId, dto));
    }

    @GetMapping("/schools")
    public ResponseEntity<PageResponseDTO<School>> getSchools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(schoolService.getSchoolsPage(pageable, search));
    }

    @PostMapping("/schools")
    public ResponseEntity<School> createSchool(@Valid @RequestBody CreateSchoolDTO dto) {
        return ResponseEntity.ok(schoolService.create(dto));
    }

    @DeleteMapping("/schools/{schoolId}")
    public ResponseEntity<Void> deleteSchool(@PathVariable Long schoolId) {
        schoolService.delete(schoolId);
        return ResponseEntity.noContent().build();
    }
}
