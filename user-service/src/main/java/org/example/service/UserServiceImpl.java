package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.audit.AuditCreateDTO;
import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.audit.Type;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.service.api.IAuditSenderService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Validated
@Service
public class UserServiceImpl implements IUserService {


    private IUserRepository userRepository;


    private ConversionService conversionService;

    private IAuditSenderService auditSenderService;


    private AuditUserDTO dummyUser = new AuditUserDTO(
            UUID.randomUUID(), "mail@mail.ru","dummyFio", UserRole.ADMIN
    );


    public UserServiceImpl(IUserRepository userRepository, ConversionService conversionService, IAuditSenderService auditSenderService) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
        this.auditSenderService = auditSenderService;
    }

    @Override
    public void save(@Valid UserCreateDTO userCreateDTO) {


        User toRegister = conversionService.convert(
                userCreateDTO, User.class
        );
        toRegister.setUuid(UUID.randomUUID());


        try {
            User save = userRepository.save(toRegister);
        } catch (Exception e) {

            StructuredException structuredException = new StructuredException();

            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


        try {

            ResponseEntity<?> someText = auditSenderService.createAudit(new AuditCreateDTO(
                    this.dummyUser, "user was created", Type.USER
            ));


        } catch (Exception e) {

            e.printStackTrace();
        }



    }


    @Override
    public User getUserById(UUID uuid) {
        return userRepository.findByUuid(
                uuid
        ).orElseThrow(
                () -> new GeneralException("Не найден пользователь по такому uuid")
        );
    }


    @Override
    public void updateUser(UUID uuid, LocalDateTime dt_update, @Valid UserCreateDTO userCreateDTO) {

        User toUpdate = userRepository.findByUuid(uuid).orElseThrow(
                () -> new GeneralException(
                        "Не найден пользователь с таким uuid"
                )
        );

        if (
                !Objects.equals(
                        toUpdate.getDtUpdate(),
                        dt_update
                )
        ) {
            throw new GeneralException(
                    "Версия пользователя уже была обновлена"
            );

        }

        toUpdate = updateUserParamsFromUserCreateDTO(toUpdate, userCreateDTO);


        try {

            //TODO CHECK FOR SPLIT OF EXCEPTION
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

            exception.put("page", "Номер страницы не может быть меньше 0");

        }
        if (rowsPerPage < 1) {
            exception.put("size", "Размер страницы не может быть меньше 0");

        }

        if (exception.hasExceptions()) {
            throw exception;
        }

        try {

            Page<User> page = userRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

            return page;

        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

    }

    @Override
    public int setUserActiveByEmail(String email) {
        try {

            return userRepository.setUserActiveByEmail(email);
        } catch (Exception e) {
            StructuredException structuredException = new StructuredException();
            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }

            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


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
