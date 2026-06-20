package ru.accouting.student.config;

import ru.accouting.student.service.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   DaoAuthenticationProvider authProvider) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login",
                                "/auth/login", "/auth/logout",
                                "/enter"
                        ).permitAll()
                        .requestMatchers(
                                "/lists",
                                "/student-applied/**",
                                "/student-applied",
                                "/specialties/**",
                                "/specialties",
                                "/groups", "/groups/**",
                                "/platoons", "/platoons/**",
                                "/students", "/students/**",
                                "/military-commissariats", "/military-commissariats/**",
                                "/military-accounting-specialties", "/military-accounting-specialties/**",
                                "/physical-list",
                                "/contest-protocol", "/contest-protocol/**",
                                "/student-management"
                        ).hasAnyAuthority("TECHNOLOGIST", "FULL")
                        .requestMatchers(
                                "/admin-physical", "/admin-physical/**",
                                "/admin", "/admin/users", "/admin/users/**",
                                "/exercises", "/exercises/**"
                        ).hasAnyAuthority("FULL")
                        .anyRequest().hasAnyAuthority("FULL")
                )
                .authenticationProvider(authProvider)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserServiceImpl userService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider authProvider) {
        return new ProviderManager(List.of(authProvider));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
