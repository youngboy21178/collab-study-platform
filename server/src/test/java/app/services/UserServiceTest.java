package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_ShouldReturnUser_WhenExists() {
        // GIVEN
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setName("John Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // WHEN
        User result = userService.getUser(userId);

        // THEN
        assertEquals(userId, result.getUserId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getUser_ShouldThrow_WhenNotFound() {
        // GIVEN
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        // У твоєму коді там RuntimeException
        assertThrows(RuntimeException.class, () -> userService.getUser(userId));
    }
}