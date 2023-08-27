package org.example.service.api;

import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.dao.entities.user.User;

import java.util.List;
import java.util.Set;

public interface IUserService {

    User findAndSave(UserDTO userDTO);

    User findByRoleAndSave(UserDTO userDTO, UserRole role);

    List<User> findAllAndSave(Set<UserDTO> userDTOSet);

    User findUserInCurrentContext();

    AuditUserDTO findAuditUserDTOInfoInCurrentContext();

    boolean userInCurrentContextHasOneOfRoles(UserRole... userRoles);

}
