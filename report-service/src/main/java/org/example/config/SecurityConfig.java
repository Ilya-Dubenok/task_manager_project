package org.example.config;


import jakarta.servlet.http.HttpServletResponse;
import org.example.endpoint.web.filters.FilterChainExceptionFilter;
import org.example.endpoint.web.filters.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    @Bean
    @Profile("!test")
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter filter, FilterChainExceptionFilter filterChainExceptionFilter) throws Exception {

        http = http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionConfigurer ->
                        exceptionConfigurer
                                .authenticationEntryPoint(
                                        (request, response, authException) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_UNAUTHORIZED
                                                )
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) ->
                                                response.setStatus(HttpServletResponse.SC_FORBIDDEN)
                                )
                )
                //TODO ADD URLS
                .authorizeHttpRequests(requests->requests
                        .anyRequest().hasAnyRole("ADMIN")
                )
                .addFilterBefore(
                filter,
                UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filterChainExceptionFilter,
                        JwtFilter.class);

        return http.build();
    }


    @Bean
    @Profile("test")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http, JwtFilter filter, FilterChainExceptionFilter filterChainExceptionFilter) throws Exception {

        http = http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionConfigurer ->
                        exceptionConfigurer
                                .authenticationEntryPoint(
                                        (request, response, authException) ->
                                                response.setStatus(
                                                        HttpServletResponse.SC_UNAUTHORIZED
                                                )
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) ->
                                                response.setStatus(HttpServletResponse.SC_FORBIDDEN)
                                )
                )
                //TODO ADD URLS
                .authorizeHttpRequests(requests->requests
                        .anyRequest().permitAll()
                );
//                .addFilterBefore(
//                        filter,
//                        UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(filterChainExceptionFilter,
//                        JwtFilter.class);

        return http.build();
    }

}
