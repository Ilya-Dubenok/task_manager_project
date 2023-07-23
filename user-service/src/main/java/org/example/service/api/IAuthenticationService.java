package org.example.service.api;

import org.example.core.dto.UserRegistrationDTO;
import org.example.dao.entities.verification.EmailStatus;

public interface IAuthenticationService {

    Integer registerUser(UserRegistrationDTO userRegistrationDTO);

    void verifyUserWithCode(Integer code, String mail);

}
