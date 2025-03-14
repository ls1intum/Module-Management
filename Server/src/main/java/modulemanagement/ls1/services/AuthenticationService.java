package modulemanagement.ls1.services;

import modulemanagement.ls1.enums.UserRole;
import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser(Jwt jwt) {
        UUID uuid = getUuidFromJwt(jwt);
        return userRepository.findById(uuid)
                .orElseGet(() -> createUserFromJwt(jwt, uuid));
    }

    private UUID getUuidFromJwt(Jwt jwt) {
        return UUID.fromString((String)jwt.getClaims().get("sub"));
    }

    private User createUserFromJwt(Jwt jwt, UUID uuid) {
        User newUser = new User();
        newUser.setUserId(uuid);

        Map<String, Object> claims = jwt.getClaims();

        newUser.setUserName((String) claims.getOrDefault("preferred_username", ""));
        newUser.setFirstName((String) claims.getOrDefault("given_name", ""));
        newUser.setLastName((String) claims.getOrDefault("family_name", ""));
        newUser.setEmail((String) claims.getOrDefault("email", ""));

        newUser.setRole(UserRole.UNDEFINED);

        return userRepository.save(newUser);
    }
}
