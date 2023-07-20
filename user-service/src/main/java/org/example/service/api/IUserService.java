package org.example.service.api;

import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.dao.entities.user.User;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IUserService {


    void save(UserCreateDTO userCreateDTO);


    User getUserById(UUID uuid);

    void updateUser(UUID uuid, LocalDateTime dt_update, UserCreateDTO userCreateDTO);

    Page<User> getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage);

    int setUserActiveByEmail(String email);
}
