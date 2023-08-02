package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserLoginDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.dao.entities.user.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IUserService {


    void save(@Valid UserCreateDTO userCreateDTO);

    void save(@Valid UserRegistrationDTO userRegistrationDTO);


    User getUserById(UUID uuid);

    void updateUser(UUID uuid, LocalDateTime dt_update,@Valid UserCreateDTO userCreateDTO);

    Page<User> getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage);

    int setUserActiveByEmail(String email);

    User login(@Valid UserLoginDTO userLoginDTO);

    String loginAndReceiveToken(@Valid UserLoginDTO userLoginDTO);

    User getUserFromCurrentSecurityContext()  throws UsernameNotFoundException;
}
