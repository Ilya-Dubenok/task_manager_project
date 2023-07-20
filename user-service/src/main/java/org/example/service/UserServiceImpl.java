package org.example.service;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.example.core.dto.UserCreateDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DataBaseExceptionsMapper;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements IUserService {


    private IUserRepository userRepository;


    private ConversionService conversionService;


    public UserServiceImpl(IUserRepository userRepository, ConversionService conversionService) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
    }

    @Override
    public void save(UserCreateDTO userCreateDTO) {


        //TODO ADD TO CONVERTER
        User toRegister = new User(UUID.randomUUID());
        toRegister.setMail(userCreateDTO.getMail());
        toRegister.setRole(userCreateDTO.getRole());
        toRegister.setFio(userCreateDTO.getFio());
        toRegister.setStatus(userCreateDTO.getStatus());
        toRegister.setPassword(userCreateDTO.getPassword());

        // TODO  ADD EXCEPTION HANDLING
        try {
            userRepository.save(toRegister);
        } catch (Exception e) {
            StructuredException structuredException = new
                    StructuredException();
            if (DataBaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


    }


    @Override
    public User getUserById(UUID uuid) {
        return userRepository.findByUuid(
                uuid
        ).orElse(null);
    }



    @Override
    public void updateUser(UUID uuid, LocalDateTime dt_update, UserCreateDTO userCreateDTO) {

        User toUpdate = userRepository.findByUuid(uuid).orElseThrow(
                () -> new StructuredException(
                        "uuid", "Не найден пользователь с таким uuid"
                )
        );

        if (
                !Objects.equals(
                        toUpdate.getDtUpdate(),
                        dt_update
                )
        ) {
            //TODO CHANGE EXCEPTION HANDLING
            throw new StructuredException(
                    "dt_update", "Версия пользователя уже была обновлена"
            );

        }

        toUpdate = updateUserParamsFromUserCreateDTO(toUpdate, userCreateDTO);


        try {
            //TODO CHANGE EXCEPTION HANDLING DEPENDING ON CONSTRAINTS

            userRepository.save(toUpdate);
        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


    }


    @Override
    public Page<User> getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage) {

        StructuredException exception = new StructuredException();

        if (currentRequestedPage < 0) {

            exception.put("page","Номер страницы не может быть меньше 0");

        }
        if (rowsPerPage < 1) {
            exception.put("size", "Размер страницы не может быть меньше 0");

        }

        if (exception.hasExceptions()) {
            throw exception;
        }

        try {

            // TODO ADD EXCEPTION HANDLING
            Page<User> page = userRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

            return page;

        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

    }

    @Override
    public int setUserActiveByEmail(String email) {

        return userRepository.setUserActiveByEmail(email);


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

    private static void updateUserParamsWithPassedArguments(String mail, String fio, String password, User toRegister) {
        toRegister.setMail(mail);
        toRegister.setPassword(password);
        toRegister.setFio(fio);
        toRegister.setRole(UserRole.USER);
        toRegister.setStatus(UserStatus.WAITING_ACTIVATION);
    }


}
