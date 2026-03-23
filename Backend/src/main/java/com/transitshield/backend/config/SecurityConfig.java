package com.transitshield.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Admin-only domain management
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/buses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/buses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/buses/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/routes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/routes/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/route-variants/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/route-variants/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/route-variants/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/route-variant-stops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/route-variant-stops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/route-variant-stops/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/stops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/stops/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/stops/**").hasRole("ADMIN")

                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // Authenticated read access for shared reference data
                .requestMatchers(HttpMethod.GET, "/api/buses/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/routes/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/route-variants/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/route-variant-stops/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/stops/**").authenticated()

                // Passenger-only features
                .requestMatchers("/api/rewards/**").hasRole("PASSENGER")
                .requestMatchers("/api/qr/scan").hasRole("PASSENGER")
                .requestMatchers("/api/trips/**").hasRole("PASSENGER")
                .requestMatchers("/api/tasks/**").hasRole("PASSENGER")

                // Passenger + Driver lost & found reporting
                .requestMatchers("/api/lost-items/**").hasAnyRole("PASSENGER", "DRIVER")

                // Driver-only features
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                .requestMatchers(HttpMethod.POST, "/api/location/update").hasRole("DRIVER")

                // Shared authenticated live tracking / dashboard reads
                .requestMatchers(HttpMethod.GET, "/api/location/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/dashboard").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.*:*",
            "http://10.0.2.2:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
