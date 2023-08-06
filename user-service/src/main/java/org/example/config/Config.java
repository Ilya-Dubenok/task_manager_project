package org.example.config;


import jakarta.persistence.Id;
import org.example.config.property.ApplicationProperties;
import org.example.config.property.JWTProperties;
import org.example.dao.entities.user.User;
import org.example.utils.ChangedFieldOfEntitySearcher;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


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

    @Bean
    public ChangedFieldOfEntitySearcher<User> changedFieldOfEntitySearcher() {

        return new ChangedFieldOfEntitySearcher
                .Builder<>(User.class)
                .setNotToScanAnnotations(List.of(
                        Id.class, CreatedDate.class
                ))
                .setFieldsWithNoValuesToDisclose(List.of("password"))
                .build();


    }




}
