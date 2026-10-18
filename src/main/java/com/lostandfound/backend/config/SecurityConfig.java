package com.lostandfound.backend.config;

import com.lostandfound.backend.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    //scrambles (hashes) passwords before saving them, so nobody can read them if the database leaks.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                //csrf.disable()turns off a browser-cookie protection you don’t need, because your
                // apps use tokens instead.

                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                //STATELESS the server doesn’t remember logins. Each request must carry its own token (JWT).
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                writeError(res, 401, "Not logged in, or your session has expired"))
                        .accessDeniedHandler((req, res, ex) ->
                                writeError(res, 403, "You do not have permission to do this"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/me").authenticated()
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
                        .requestMatchers("/v1/admin/**").hasRole("Admin")
                        .requestMatchers("/v1/moderation/**").hasAnyRole("Moderator", "Admin")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    // Lets the web admin (browser) call the API. The mobile app is not affected by CORS.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrl) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(frontendUrl));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static void writeError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}