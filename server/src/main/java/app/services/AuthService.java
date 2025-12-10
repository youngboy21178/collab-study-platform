package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import app.dto.auth.LoginRequest;
import app.dto.auth.RegisterRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Map<String, Long> activeTokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    public String generateTokenForUser(User user) {
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, user.getUserId());
        return token;
    }

    public User getUserByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is missing");
        }

        Long userId = activeTokens.get(token);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            activeTokens.remove(token);
        }
    }
}