package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import app.dto.auth.LoginRequest;
import app.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Вмикаємо Mockito
class AuthServiceTest {

    @Mock
    private UserRepository userRepository; // Імітація репозиторія

    @Mock
    private BCryptPasswordEncoder passwordEncoder; // Імітація енкодера паролів

    @InjectMocks
    private AuthService authService; // Сервіс, який ми тестуємо (сюди вставляться моки)

    @Test
    void register_ShouldSaveUser_WhenEmailIsUnique() {
        // GIVEN (Дано)
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");
        request.setPassword("password123");

        // Налаштовуємо мок: коли питають, чи існує емейл -> кажемо "ні"
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_pass");

        // WHEN (Коли)
        authService.register(request);

        // THEN (Тоді)
        // Перевіряємо, що метод save викликався 1 раз
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // GIVEN
        RegisterRequest request = new RegisterRequest();
        request.setEmail("busy@example.com");

        // Налаштовуємо мок: емейл вже є
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // WHEN & THEN
        // Очікуємо помилку
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(request);
        });

        // Перевіряємо, що збереження НЕ викликалось
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnUser_WhenCredentialsAreCorrect() {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("secret");

        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setPasswordHash("hashed_secret");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("secret", "hashed_secret")).thenReturn(true);

        // WHEN
        User result = authService.login(request);

        // THEN
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void login_ShouldThrow_WhenPasswordWrong() {
        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong_pass");

        User mockUser = new User();
        mockUser.setPasswordHash("hashed_secret");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_pass", "hashed_secret")).thenReturn(false);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}