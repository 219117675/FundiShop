package za.ac.cput.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import za.ac.cput.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------
                // Disabled because this is a REST API using JWT.
                .csrf(csrf -> csrf.disable())

                // -------------------------------------------------
                // CORS
                // -------------------------------------------------
                .cors(cors -> {})

                // -------------------------------------------------
                // SESSION MANAGEMENT
                // -------------------------------------------------
                // JWT authentication is stateless.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // -------------------------------------------------
                // AUTHORIZATION
                // -------------------------------------------------
                .authorizeHttpRequests(auth -> auth

                        // -----------------------------------------
                        // Authentication
                        // -----------------------------------------
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // -----------------------------------------
                        // User registration
                        // -----------------------------------------
                        .requestMatchers(
                                "/api/users/register"
                        ).permitAll()

                        // -----------------------------------------
                        // Public Fundi endpoints
                        // -----------------------------------------
                        .requestMatchers(
                                "/api/fundis/**",
                                "/api/fundiprofiles/**"
                        ).permitAll()

                        // -----------------------------------------
                        // Public Review endpoints
                        // -----------------------------------------
                        // Reviews can be viewed by anyone browsing
                        // Fundis on the platform.
                        .requestMatchers(
                                "/api/reviews",
                                "/api/reviews/**"
                        ).permitAll()

                        // -----------------------------------------
                        // Notifications
                        // -----------------------------------------
                        // Notifications belong to authenticated
                        // users, so they must NOT be public.
                        .requestMatchers(
                                "/api/notifications/**"
                        ).authenticated()

                        // -----------------------------------------
                        // Everything else
                        // -----------------------------------------
                        .anyRequest().authenticated()
                )

                // -------------------------------------------------
                // JWT FILTER
                // -------------------------------------------------
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}