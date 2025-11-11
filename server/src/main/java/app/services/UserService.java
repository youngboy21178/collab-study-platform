package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import app.dto.auth.UpdateUserProfileRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User updateProfile(Long userId, UpdateUserProfileRequest request) {
        User user = getUser(userId);
        user.setName(request.getName());
        user.setAvatarUrl(request.getAvatarUrl());
        return userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
