package org.example.service.api;

import org.example.core.dto.UserRegistrationDTO;

public interface IAuthenticationService {

    Integer registerUser(UserRegistrationDTO userRegistrationDTO);

    void verifyUserWithCode(Integer code, String mail);

}
