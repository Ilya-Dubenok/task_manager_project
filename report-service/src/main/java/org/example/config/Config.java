package org.example.config;

import org.example.config.properties.ApplicationProperties;
import org.example.config.properties.JWTProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
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

}
