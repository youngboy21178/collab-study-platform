package app.api;

import app.db.entities.User;
import app.dto.auth.AuthResponse;
import app.dto.auth.LoginRequest;
import app.dto.auth.RegisterRequest;
import app.services.AuthService;
import app.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = authService.login(request);
        String token = authService.generateTokenForUser(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getUserId()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        User user = authService.getUserByToken(token);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> handleOAuth2Success(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(401).build();
        }

        String email = oauth2User.getAttribute("email");

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

        String token = authService.generateTokenForUser(user);

        return ResponseEntity.ok(new AuthResponse(token, user.getUserId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}