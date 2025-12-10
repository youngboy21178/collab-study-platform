package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import app.dto.auth.UpdateUserProfileRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Пошук по email (для AuthController)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Отримання юзера по ID
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Сумісність (якщо десь викликається getUserById)
    public User getUserById(Long id) {
        return getUser(id);
    }

    // --- ДОДАНИЙ МЕТОД SAVE ---
    public User save(User user) {
        return userRepository.save(user);
    }
    // --------------------------

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateProfile(Long userId, UpdateUserProfileRequest request) {
        User user = getUser(userId);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        return userRepository.save(user);
    }
}