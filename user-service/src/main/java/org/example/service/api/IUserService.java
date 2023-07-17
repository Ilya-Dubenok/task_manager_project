package org.example.service.api;

import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.dao.entities.user.User;

import java.util.UUID;

public interface IUserService {


    void saveFromApiSource(UserCreateDTO userCreateDTO);

    Integer saveFromUserSouce(UserRegistrationDTO userRegistrationDTO);

    void verifyUser(String email, Integer verificationCode);

    User getUserById(UUID uuid);

    void updateUser(UUID uuid, Long dt_update, UserCreateDTO userCreateDTO);

    PageOfUserDTO getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage);
}
