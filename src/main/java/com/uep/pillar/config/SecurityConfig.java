package com.uep.pillar.config;

import com.uep.pillar.security.JwtAuthFilter;
import com.uep.pillar.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security configuration for JWT-based authentication.
 * Configures authentication filter, security filter chain, and CORS.
 * 
 * Production Deployment:
 * - Set CORS_ALLOWED_ORIGINS environment variable to your frontend URLs
 * - Example: CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
 * - Or set cors.allowed-origins in application-prod.properties
 * 
 * Security Features:
 * - Stateless JWT-based authentication
 * - CSRF disabled for REST API (stateless)
 * - Role-based authorization via @PreAuthorize
 * - Public read endpoints for articles, categories, tags, publication issues
 * - Authenticated endpoints for write operations
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * CORS allowed origins configuration.
     * Configure via environment variable CORS_ALLOWED_ORIGINS or cors.allowed-origins property.
     * Format: comma-separated list of URLs (e.g., "https://domain.com,https://www.domain.com")
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String[] allowedOrigins;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF is disabled for stateless JWT-based API endpoints
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/error", "/actuator/health"))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Public read endpoints
                .requestMatchers("GET", "/api/articles/**").permitAll()
                .requestMatchers("GET", "/api/categories/**").permitAll()
                .requestMatchers("GET", "/api/tags/**").permitAll()
                .requestMatchers("GET", "/api/publication-issues/**").permitAll()
                .requestMatchers("GET", "/api/search/**").permitAll()
                .requestMatchers("GET", "/api/media/**").permitAll() // Public media access
                .requestMatchers("POST", "/api/articles/**/views").permitAll() // View count increment
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration for frontend integration.
     * 
     * Production Setup:
     * 1. Set CORS_ALLOWED_ORIGINS environment variable with your frontend URLs
     * 2. Or configure cors.allowed-origins in application-prod.properties
     * 
     * Allowed:
     * - Methods: GET, POST, PUT, DELETE, OPTIONS
     * - Headers: All headers (*)
     * - Credentials: Enabled (for JWT cookies if needed)
     * - Max Age: 3600 seconds (1 hour)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // CORS origins are configurable via environment variable CORS_ALLOWED_ORIGINS
        // or cors.allowed-origins property in application.properties/application-prod.properties
        // Default: http://localhost:5173,http://localhost:3000 for development
        // IMPORTANT: Override this in production with your frontend domain(s)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
