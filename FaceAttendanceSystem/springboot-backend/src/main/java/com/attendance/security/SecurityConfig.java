package com.attendance.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/api/auth/login").permitAll()
                // TEMP: open so the very first admin can be created.
                // Consider locking this back down (hasRole("ADMIN")) once
                // you have at least one real admin account.
                .requestMatchers("/api/auth/register").permitAll()

                // Internal service-to-service calls from the Python side -
                // protected by their own shared-secret header, not JWT.
                .requestMatchers("/api/attendance/recognize", "/api/attendance/unknown-detection").permitAll()
                .requestMatchers("/api/students/*/face-encoding").permitAll()

                // Admin-only / Admin+Teacher
                .requestMatchers("/api/analytics/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/unknown-detections/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/teachers/**").hasRole("ADMIN")
                .requestMatchers("/api/attendance/report").hasAnyRole("ADMIN", "TEACHER")
                .requestMatchers("/api/attendance/start-session", "/api/attendance/stop-session").hasAnyRole("ADMIN", "TEACHER")

                // Any authenticated user (students viewing their own stats etc.)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
