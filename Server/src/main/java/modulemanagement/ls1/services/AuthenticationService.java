package modulemanagement.ls1.services;

import modulemanagement.ls1.models.User;
import modulemanagement.ls1.repositories.UserRepository;
import modulemanagement.ls1.shared.ResourceNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found."));
    }


    private UUID getUuidFromJwt(Jwt jwt) {
        return UUID.fromString((String)jwt.getClaims().get("sub"));
    }

}
