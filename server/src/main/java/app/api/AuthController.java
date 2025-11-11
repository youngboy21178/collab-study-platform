package app.api;

import app.db.entities.User;
import app.dto.auth.LoginRequest;
import app.dto.auth.RegisterRequest;
import app.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok("registered");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = authService.login(request);
            // поки що повернемо просту відповідь; потім тут можна буде зробити JWT
            return ResponseEntity.ok("login ok, userId=" + user.getUserId());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
}
