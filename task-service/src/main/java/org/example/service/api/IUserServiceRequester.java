package org.example.service.api;

import org.example.core.dto.user.UserDTO;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IUserServiceRequester {

    UserDTO getUser(UUID uuid);
    Set<UserDTO> getSetOfUserDTO(List<UUID> uuids);
}
