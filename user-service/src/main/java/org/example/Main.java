package org.example;

import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.UUID;


public class Main  {

    public static void main(String[] args) {

        ApplicationContext context =
                SpringApplication.run(Config.class);
        IUserRepository bean = context.getBean(IUserRepository.class);
        User save = bean.save(new User(UUID.randomUUID()));
        System.out.println(save);

        User user = bean.findByUuid(UUID.fromString("ece0439c-2916-44b8-b640-a90592181aaf")).orElseThrow();
        System.out.println(user.getUuid());

        List<User> users = bean.findAll();

        System.out.println(users.size());

    }
}
