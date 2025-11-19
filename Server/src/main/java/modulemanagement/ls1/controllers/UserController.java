package modulemanagement.ls1.controllers;

import modulemanagement.ls1.dtos.UserDTO;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.shared.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUser User user) {
        UserDTO userDTO = UserDTO.fromUser(user);
        return ResponseEntity.ok(userDTO);
    }
}
