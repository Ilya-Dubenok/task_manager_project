package org.example.service;

import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRole;
import org.example.core.exception.GeneralException;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.example.service.api.IUserServiceRequester;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                    userRepository.save(res);
                    return res;
                }
        );
    }

    @Override
    public User findByRoleAndSave(UserDTO userDTO, UserRole role) {
        UserDTO dtoRes = userServiceRequester.getUser(userDTO.getUuid());

        if (null == dtoRes) {
            throw new GeneralException("передан uuid не существующего пользователя");
        }

        if (!dtoRes.getRole().equals(role)) {
            throw new GeneralException("данный пользователь не " + role.toString().toLowerCase());
        }

        User res = new User();
        res.setUuid(dtoRes.getUuid());
        userRepository.save(res);
        return res;
    }

    @Override
    public List<User> findAllAndSave(List<UserDTO> userDTOList) {
        List<UUID> listOfUUIDs = userDTOList.stream().map(UserDTO::getUuid).toList();

        List<User> found = userRepository.findAllById(listOfUUIDs);

        if (userDTOList.size() != found.size()) {

            List<UUID> toFindOnService = userDTOList.stream()
                    .map(UserDTO::getUuid)
                    .filter(uuid ->
                            found.stream().anyMatch(user -> user.getUuid().equals(uuid)))
                    .toList();

            Set<UserDTO> setOfUserDTO = userServiceRequester.getSetOfUserDTO(toFindOnService);

            List<User> toSaveAdditionally = setOfUserDTO.stream().map(x -> new User(x.getUuid())).toList();

            userRepository.saveAll(toSaveAdditionally);

            if (found.size() + toSaveAdditionally.size() < userDTOList.size()) {

                throw new GeneralException("Переданы не существующие пользователи");
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
}
