package com.medical.center.leo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
                UserDetails user = User.withDefaultPasswordEncoder() // Solo para la prueba, se usa un PasswordEncoder
                                                                     // si fuera real
                                .username("user")
                                .password("password")
                                .roles("USER")
                                .build();
                UserDetails admin = User.withDefaultPasswordEncoder()
                                .username("admin")
                                .password("adminpass")
                                .roles("ADMIN", "USER")
                                .build();
                return new InMemoryUserDetailsManager(user, admin);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST si no usas
                                                              // sesiones/cookies
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**")
                                                .permitAll() // Permitir
                                                             // Swagger
                                                .requestMatchers(HttpMethod.POST, "/api/doctores", "/api/consultorios")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/doctores/**",
                                                                "/api/consultorios/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/doctores/**",
                                                                "/api/consultorios/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                                                .anyRequest().authenticated() // Cualquier otra petición requiere
                                                                              // autenticación
                                )
                                .httpBasic(withDefaults()); // Usar HTTP Basic
                return http.build();
        }
}