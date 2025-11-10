package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // усі наші REST-ендпойнти відкриті
                        .anyRequest().permitAll()              // решту теж поки що пускаємо
                )
                .formLogin(form -> form.disable())            // ❌ вимикаємо HTML-форму логіну
                .httpBasic(basic -> basic.disable());         // і базову авторизацію теж

        return http.build();
    }
}
