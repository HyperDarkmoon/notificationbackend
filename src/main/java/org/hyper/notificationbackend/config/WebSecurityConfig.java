package org.hyper.notificationbackend.config;

import org.hyper.notificationbackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(userService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Static file access - allow public access to uploaded files
                        .requestMatchers("/uploads/**").permitAll()
                        // TV content viewing endpoints - public access for TVs
                        .requestMatchers("/api/content/tv/**").permitAll()
                        // Profile endpoints for TVs - public access
                        .requestMatchers("/api/profiles/tv/**").permitAll()
                        // File upload endpoints - permit all to avoid CORS issues
                        .requestMatchers(HttpMethod.POST, "/api/content/upload-file").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/content/upload-files").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/content/from-request").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/content/delete-file/**").hasRole("ADMIN")
                        // Content management endpoints - require admin role
                        .requestMatchers(HttpMethod.GET, "/api/content/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/content/active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/content/upcoming").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/content").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/content/restore-disabled").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/content/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/content/**").hasRole("ADMIN")
                        .requestMatchers("/api/content/**").hasRole("ADMIN")
                        // Profile management endpoints - require admin role
                        .requestMatchers(HttpMethod.GET, "/api/profiles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/profiles").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/profiles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/profiles/assignments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/profiles/assign").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/profiles/assignments/**").hasRole("ADMIN")
                        // TV Management endpoints - admin only for CRUD operations
                        .requestMatchers(HttpMethod.GET, "/api/tvs/active").permitAll() // Public access for frontend TV selection
                        .requestMatchers(HttpMethod.GET, "/api/tvs/name/**").permitAll() // Public access for TV lookup
                        .requestMatchers(HttpMethod.GET, "/api/tvs/check/**").permitAll() // Public access for TV status check
                        .requestMatchers(HttpMethod.POST, "/api/tvs").hasRole("ADMIN") // Admin only for creating TVs
                        .requestMatchers(HttpMethod.PUT, "/api/tvs/**").hasRole("ADMIN") // Admin only for updating TVs
                        .requestMatchers(HttpMethod.DELETE, "/api/tvs/**").hasRole("ADMIN") // Admin only for deleting TVs
                        .requestMatchers(HttpMethod.GET, "/api/tvs").hasRole("ADMIN") // Admin only for listing all TVs
                        .requestMatchers(HttpMethod.GET, "/api/tvs/**").hasRole("ADMIN") // Admin only for detailed TV info
                        .requestMatchers(HttpMethod.POST, "/api/tvs/initialize-defaults").hasRole("ADMIN") // Admin only for initialization
                        .requestMatchers("/api/tvs/search/**").hasRole("ADMIN") // Admin only for TV search
                        // Legacy TV enum endpoints - keep for backward compatibility
                        .requestMatchers("/api/tv/**").permitAll()
                        // Other admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(httpBasic -> {});

        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow both localhost and network IP for development and testing
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",           // For local development
            "http://172.16.1.12:3000",
            "http://10.41.15.227:3000",         // For network access
            "http://127.0.0.1:3000"            // Alternative localhost
        )); 
        // Alternative: Use setAllowedOriginPatterns for more flexible matching
        // configuration.setAllowedOriginPatterns(List.of("http://172.16.1.*:3000", "http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowCredentials(true); // Set to true for authenticated requests
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers for file uploads
        configuration.setExposedHeaders(List.of("Authorization", "X-Auth-Token"));
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
