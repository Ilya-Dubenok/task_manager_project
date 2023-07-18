package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@EnableJpaRepositories
@EnableTransactionManagement
@EnableJpaAuditing
@EnableWebMvc
@SpringBootApplication
public class Main  {

    public static void main(String[] args) throws JsonProcessingException {

//        ApplicationContext context = SpringApplication.run(Main.class);
//        IUserService iUserService = context.getBean(IUserService.class);
//
//        IEmailService emailService = context.getBean(IEmailService.class);
//
////        WebApplicationInitializer bean = context.getBean(WebApplicationInitializer.class);

        SpringApplication.run(Main.class, args);
    }
}
