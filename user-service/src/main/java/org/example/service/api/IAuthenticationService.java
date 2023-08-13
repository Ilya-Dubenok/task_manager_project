package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.user.UserLoginDTO;
import org.example.core.dto.user.UserRegistrationDTO;

public interface IAuthenticationService {

    Integer registerUser(@Valid UserRegistrationDTO userRegistrationDTO);

    void verifyUserWithCode(Integer code, String mail);

    void setEmailDeliveryStatus(String mail, Boolean status);

    String loginAndReceiveToken(UserLoginDTO userLoginDTO);

}
