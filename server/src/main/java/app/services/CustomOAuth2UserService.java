package app.services;

import app.db.entities.User;
import app.db.repositories.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub"); // 'sub' - це унікальний ID у Google

        // Перевіряємо, чи є користувач у базі
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Реєструємо нового користувача
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGoogleId(googleId);
            user.setPasswordHash(null); // Пароля немає
            // user.setAvatarUrl(oAuth2User.getAttribute("picture")); // Можна додати аватарку
            userRepository.save(user);
        } else {
            // Якщо користувач вже є, можна оновити йому googleId, якщо його не було
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
        }

        return oAuth2User;
    }
}