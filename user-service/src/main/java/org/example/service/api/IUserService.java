package org.example.service.api;

import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.dao.entities.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IUserService {


    boolean saveFromApiSource(UserCreateDTO userCreateDTO);

    boolean saveFromUserSouce(UserRegistrationDTO userRegistrationDTO);

    User getUserById(UUID uuid);

    boolean updateUser(UUID uuid, Long dt_update, UserCreateDTO userCreateDTO);

    PageOfUserDTO getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage);
}
