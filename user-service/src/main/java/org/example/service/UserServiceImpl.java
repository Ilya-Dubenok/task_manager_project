package org.example.service;

import org.example.core.dto.UserCreateDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
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
import java.util.Objects;
import java.util.UUID;


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


        UserRole role = userCreateDTO.getRole();
        UserStatus status = userCreateDTO.getStatus();
        StructuredException structuredException = new StructuredException();

        if (role == null) {
            structuredException.put(
                    "role","Не передана роль пользователя"
            );
        }
        if (status == null) {
            structuredException.put(
                    "status", "Не передан статус пользователя"
            );
        }

        if (structuredException.hasExceptions()) {
            throw structuredException;
        }

        User toRegister = conversionService.convert(
                userCreateDTO, User.class
        );
        toRegister.setUuid(UUID.randomUUID());

        // TODO  ADD EXCEPTION HANDLING
        try {
            userRepository.save(toRegister);
        } catch (Exception e) {

            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
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

            userRepository.save(toUpdate);
        } catch (Exception e) {
            StructuredException structuredException = new StructuredException();
            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

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



}
