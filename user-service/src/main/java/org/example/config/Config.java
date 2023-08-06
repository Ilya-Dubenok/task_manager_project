package org.example.config;


import org.example.config.property.ApplicationProperties;
import org.example.config.property.JWTProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class Config implements WebMvcConfigurer {

    @Bean
    @ConfigurationProperties("app")
    public ApplicationProperties getProperty() {
        return new ApplicationProperties();
    }

    @Bean
    @ConfigurationProperties("jwt")
    public JWTProperties getJWTProperties() {
        return new JWTProperties();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



}
