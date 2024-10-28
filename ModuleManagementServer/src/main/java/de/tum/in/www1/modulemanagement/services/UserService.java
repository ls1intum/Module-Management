package de.tum.in.www1.modulemanagement.services;

import de.tum.in.www1.modulemanagement.models.User;
import de.tum.in.www1.modulemanagement.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
