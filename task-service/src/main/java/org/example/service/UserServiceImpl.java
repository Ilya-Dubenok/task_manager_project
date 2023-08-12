package org.example.service;

import jakarta.validation.ConstraintViolationException;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.GeneralException;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.example.service.api.IUserServiceRequester;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    private IUserRepository userRepository;

    private IUserServiceRequester userServiceRequester;

    private UserHolder userHolder;

    public UserServiceImpl(IUserRepository userRepository, IUserServiceRequester userServiceRequester, UserHolder userHolder) {
        this.userRepository = userRepository;
        this.userServiceRequester = userServiceRequester;
        this.userHolder = userHolder;
    }


    @Override
    public User findAndSave(UserDTO userDTO) {
        return userRepository.findById(userDTO.getUuid()).orElseGet(
                () -> {
                    UserDTO dtoRes = userServiceRequester.getUser(userDTO.getUuid());

                    if (null == dtoRes) {
                        throw new GeneralException("передан uuid не существующего пользователя");
                    }

                    User res = new User();
                    res.setUuid(dtoRes.getUuid());
                    userRepository.saveAndFlush(res);
                    return res;
                }
        );
    }

    @Override
    public User findByRoleAndSave(UserDTO userDTO, UserRole role) {
        UserDTO dtoRes = userServiceRequester.getUser(userDTO.getUuid());

        if (null == dtoRes) {
            throw new ConstraintViolationException("передан uuid не существующего пользователя",null);
        }

        if (null == dtoRes.getRole()) {
            throw new GeneralException("произошла ошибка");
        }

        if (!dtoRes.getRole().equals(role)) {
            throw new ConstraintViolationException("данный пользователь не " + role.toString().toLowerCase(), null);
        }

        User res = new User();

        res.setUuid(dtoRes.getUuid());

        userRepository.saveAndFlush(res);

        return res;
    }

    @Override
    public List<User> findAllAndSave(Set<UserDTO> userDTOList) {
        List<UUID> listOfUUIDs = userDTOList.stream().map(UserDTO::getUuid).toList();

        List<User> found = userRepository.findAllById(listOfUUIDs);

        if (userDTOList.size() != found.size()) {


            List<UUID> toFindOnService;

            if (found.size() == 0) {

                toFindOnService = listOfUUIDs;

            } else {
               toFindOnService = userDTOList.stream()
                        .map(UserDTO::getUuid)
                        .filter(uuid ->found.stream().noneMatch(user -> user.getUuid().equals(uuid)))
                        .toList();
            }


            Set<UserDTO> setOfUserDTO = userServiceRequester.getSetOfUserDTOs(toFindOnService);

            List<User> toSaveAdditionally = setOfUserDTO.stream().map(x -> new User(x.getUuid())).toList();

            userRepository.saveAllAndFlush(toSaveAdditionally);

            if (found.size() + toSaveAdditionally.size() < userDTOList.size()) {

                throw new ConstraintViolationException("Переданы не существующие пользователи", null);
            }

            found.addAll(toSaveAdditionally);
        }

        return found;


    }

    @Override
    public User findUserInCurrentContext() {

        User user = new User();

        UUID uuid = UUID.fromString(userHolder.getUser().getUsername());

        user.setUuid(uuid);

        return user;
    }

    @Override
    public boolean isUserInCurrentContextHasOneOfRoles(UserRole... userRoles) {

        for (GrantedAuthority authority : userHolder.getUser().getAuthorities()) {

            for (UserRole userRole : userRoles) {
                if (authority.getAuthority().equals("ROLE_".concat(userRole.toString()))) {
                    return true;
                }

            }

        }

        return false;

    }
}
