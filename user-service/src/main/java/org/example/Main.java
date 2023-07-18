package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableJpaRepositories
@EnableTransactionManagement
@EnableJpaAuditing
@EnableWebMvc
@EnableConfigurationProperties
@SpringBootApplication
public class Main  {

    public static void main(String[] args) throws JsonProcessingException {
        SpringApplication.run(Main.class, args);
    }
}

