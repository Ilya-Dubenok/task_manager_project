package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserDTO;
import org.example.core.dto.utils.UserEntityToDTOConverter;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.IUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.UUID;

@ComponentScan
@EnableJpaRepositories
@EnableTransactionManagement
@EnableJpaAuditing
public class Main  {

    public static void main(String[] args) throws JsonProcessingException {

        ApplicationContext context = SpringApplication.run(Main.class);
        IUserService iUserService = context.getBean(IUserService.class);

//        UUID uuid = UUID.fromString("a3beb0df-9653-416a-a677-669fb1d7d569");
////        UserDTO userDTO = UserEntityToDTOConverter.convertUserEntityToUserDto(
////                iUserService.getUserById(uuid)
////        );
////
////        Long dtUpdate = userDTO.getDtUpdate()-1;
//
//        UserCreateDTO userCreateDTO = new UserCreateDTO(
//                "new mail", "new fil", UserRole.USER, UserStatus.DEACTIVATED, "new password"
//
//        );
//
//        boolean b = iUserService.updateUser(uuid, 556854158L, userCreateDTO);
//        System.out.println(b);


    }
}
