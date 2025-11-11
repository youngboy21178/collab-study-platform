package app.api;

import app.db.entities.User;
import app.dto.auth.UpdateUserProfileRequest;
import app.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            User user = userService.getUser(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId,
                                           @Valid @RequestBody UpdateUserProfileRequest request) {
        try {
            User updated = userService.updateProfile(userId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
