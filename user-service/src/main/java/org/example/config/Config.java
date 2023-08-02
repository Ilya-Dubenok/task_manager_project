package org.example.config;


import org.example.config.property.ApplicationProperties;
import org.example.config.property.JWTProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("Async-");
        threadPoolTaskExecutor.setCorePoolSize(2);
        threadPoolTaskExecutor.setMaxPoolSize(6);
        threadPoolTaskExecutor.setQueueCapacity(15);
        return threadPoolTaskExecutor;
    }



}
