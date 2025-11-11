package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import app.dto.auth.LoginRequest;
import app.dto.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void register_createsUserInDatabase() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Maksym");
        request.setEmail("maksym@test.com");
        request.setPassword("secret123");

        // method returns void in your code
        authService.register(request);

        User user = userRepository.findByEmail("maksym@test.com").orElseThrow();
        assertEquals("Maksym", user.getName());
        assertNotNull(user.getPasswordHash());
        assertNotEquals("secret123", user.getPasswordHash());
    }

    @Test
    void login_returnsUserForCorrectCredentials() {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("User");
        reg.setEmail("user@test.com");
        reg.setPassword("pass123");
        authService.register(reg);

        LoginRequest login = new LoginRequest();
        login.setEmail("user@test.com");
        login.setPassword("pass123");

        // login(...) returns User in your code
        User user = authService.login(login);

        assertNotNull(user);
        assertEquals("user@test.com", user.getEmail());
    }

    @Test
    void login_throwsForWrongPassword() {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("User");
        reg.setEmail("user2@test.com");
        reg.setPassword("pass123");
        authService.register(reg);

        LoginRequest login = new LoginRequest();
        login.setEmail("user2@test.com");
        login.setPassword("wrong");

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(login));
    }
}
