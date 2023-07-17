package org.example.service;

import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.core.dto.utils.UserEntityToDTOConverter;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.IUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;


@Service
public class UserServiceImpl implements IUserService {


    private IUserRepository userRepository;

    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean saveFromApiSource(UserCreateDTO userCreateDTO) {

        User toRegister = updateUserParamsFromUserCreateDTO(
                new User(UUID.randomUUID()),
                userCreateDTO,
                UserRole.ADMIN,
                UserStatus.WAITING_ACTIVATION);

        //todo ADD EXCEPTION HANDLING

        userRepository.save(toRegister);

        return true;

    }


    @Override
    public boolean saveFromUserSouce(UserRegistrationDTO userRegistrationDTO) {


        String mail = userRegistrationDTO.getMail();
        String fio = userRegistrationDTO.getFio();
        String password = userRegistrationDTO.getPassword();

        if (null == mail || null == fio || null == password) {
            //TODO CHANGE EXCEPTION HANDLING
            throw new RuntimeException("ПЕРЕДЕЛАТЬ НА КАЖДОЕ ПОЛЕ ОТДЕЛЬНО");
        }

        User toRegister = new User(UUID.randomUUID());


        //TODO CHANGE EXCEPTION HANDLING
        toRegister.setMail(mail);
        toRegister.setPassword(password);
        toRegister.setFio(fio);
        toRegister.setRole(UserRole.USER);
        toRegister.setStatus(UserStatus.WAITING_ACTIVATION);

        User user = userRepository.save(toRegister);
        //TODO ADD MAILSERVICE SENDING

        return true;

    }


    @Override
    public User getUserById(UUID uuid) {
        return userRepository.findByUuid(
                uuid
        ).orElse(null);
    }

    @Override
    public boolean updateUser(UUID uuid, Long dt_update, UserCreateDTO userCreateDTO) {

        User toUpdate = userRepository.findByUuid(uuid).orElseThrow(
                //TODO CHANGE EXCEPTION HANDLING
                () -> new RuntimeException("ПЕРЕДЕЛАТЬ ОШИБКУ")
        );

        if (
                !Objects.equals(
                        dt_update,
                        UserEntityToDTOConverter.convertLocalDateTimeToLongInMillis(
                                toUpdate.getDtUpdate()
                        )
                )
        ) {
            //TODO CHANGE EXCEPTION HANDLING
            throw new RuntimeException("ПЕРЕДЕЛАТЬ ОШИБКУ НА СООБЩЕ");

        }

        toUpdate = updateUserParamsFromUserCreateDTO(
                toUpdate, userCreateDTO
        );


        //TODO ADD EXCEPTION HANDLING

        userRepository.save(toUpdate);


        return true;
    }


    @Override
    public PageOfUserDTO getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage) {

        if (currentRequestedPage < 0) {
            //TODO ADD EXCEPTION HANDLING
            if (rowsPerPage < 1) {
                //TODO ADD EXCEPTION HANDLING
            }
            throw new RuntimeException("ПЕРЕДЕЛАТЬ");
        }


        Window<User> userWindow = userRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));
        Long count = userRepository.count();
        PageOfUserDTO res = UserEntityToDTOConverter.convertWindofOfUsersToPageOfUserDTO(
                userWindow, count, currentRequestedPage, rowsPerPage
        );

        //TODO ADD EXCEPTION HANDLING


        return res;

    }


    private static User updateUserParamsFromUserCreateDTO(User user, UserCreateDTO userCreateDTO) {

        user.setMail(userCreateDTO.getMail());
        user.setFio(userCreateDTO.getFio());

        user.setRole(userCreateDTO.getRole());

        user.setStatus(userCreateDTO.getStatus());

        user.setPassword(userCreateDTO.getPassword());
        return user;
    }


    private static User updateUserParamsFromUserCreateDTO(User user, UserCreateDTO userCreateDTO,
                                                          UserRole defaultRole, UserStatus defaultStatus) {

        user.setMail(userCreateDTO.getMail());
        user.setFio(userCreateDTO.getFio());

        UserRole role = userCreateDTO.getRole();
        user.setRole(role == null ? defaultRole : role);

        UserStatus status = userCreateDTO.getStatus();
        user.setStatus(status == null ? defaultStatus : status);

        user.setPassword(userCreateDTO.getPassword());
        return user;
    }


}
