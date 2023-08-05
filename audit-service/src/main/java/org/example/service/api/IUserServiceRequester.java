package org.example.service.api;

import org.example.core.dto.user.UserDTO;

import java.util.UUID;

public interface IUserServiceRequester {

    UserDTO getUser(UUID uuid);

}
