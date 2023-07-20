package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.core.dto.utils.UserEntityToDTOConverter;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.service.UserServiceImpl;
import org.example.service.api.IUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@EnableJpaRepositories
@EnableTransactionManagement
@EnableJpaAuditing
@EnableConfigurationProperties
@SpringBootApplication
public class Main  {

    public static void main(String[] args) throws JsonProcessingException {
        ConfigurableApplicationContext run = SpringApplication.run(Main.class, args);


    }
}

