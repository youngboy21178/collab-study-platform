package app.api;

import app.db.entities.User;
import app.dto.auth.LoginRequest;
import app.dto.auth.RegisterRequest;
import app.services.AuthService;
import java.util.Map;
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
    private String extractToken(String authHeader, String tokenQuery) {
        if (tokenQuery != null && !tokenQuery.isBlank()) {
            return tokenQuery;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        throw new IllegalArgumentException("Token is missing");
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
            String token = authService.generateTokenForUser(user);

            // тепер повертаємо JSON
            return ResponseEntity.ok(
                    Map.of(
                            "userId", user.getUserId(),
                            "token", token
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestParam(name = "token", required = false) String tokenQuery
    ) {
        try {
            String token = extractToken(authHeader, tokenQuery);
            User user = authService.getUserByToken(token);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestParam(name = "token", required = false) String tokenQuery
    ) {
        String token;
        try {
            token = extractToken(authHeader, tokenQuery);
        } catch (IllegalArgumentException ex) {
            // якщо немає токена – можна повернути 400 або просто 200
            return ResponseEntity.badRequest().body("Token is missing");
        }

        authService.logout(token);
        return ResponseEntity.ok().build();
    }

}
