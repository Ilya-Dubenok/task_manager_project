package org.example.config;

import jakarta.servlet.http.HttpServletResponse;
import org.example.endpoint.web.filters.FilterChainExceptionFilter;
import org.example.endpoint.web.filters.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter filter, FilterChainExceptionFilter filterChainExceptionFilter) throws Exception  {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        // Set session management to stateless
        http = http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();

        // Set unauthorized requests exception handler
        http = http
                .exceptionHandling()
                .authenticationEntryPoint(
                        (request, response, ex) -> {
                            response.setStatus(
                                    HttpServletResponse.SC_UNAUTHORIZED
                            );
                        }
                )
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(
                            HttpServletResponse.SC_FORBIDDEN
                    );
                })
                .and();

        // Set permissions on endpoints
        http.authorizeHttpRequests(requests -> requests
                // Our public endpoints
                .requestMatchers("/internal/**").permitAll()
                .requestMatchers("/api/v1/users/registration").permitAll()
                .requestMatchers("/api/v1/users/verification").permitAll()
                .requestMatchers("/api/v1/users/login/**").permitAll()
                .requestMatchers("/api/v1/users/me").authenticated()
                .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN")
                .anyRequest().authenticated()

        );

        // Add JWT token filter
        http.addFilterBefore(
                filter,
                UsernamePasswordAuthenticationFilter.class
        );

        http.addFilterBefore(
                filterChainExceptionFilter,
                JwtFilter.class
        );

        return http.build();
    }

}
