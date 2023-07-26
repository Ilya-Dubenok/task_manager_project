package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.user.UserRegistrationDTO;

public interface IAuthenticationService {

    Integer registerUser(@Valid UserRegistrationDTO userRegistrationDTO);

    void verifyUserWithCode(Integer code, String mail);

}
