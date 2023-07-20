package org.example.service;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.IEmailService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements IUserService {


    private IUserRepository userRepository;

    private IEmailService emailService;

    private final ConversionService conversionService;

    private Map<String, Integer> codeHolder = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(5, TimeUnit.MINUTES)
            .build();

    public UserServiceImpl(IUserRepository userRepository, IEmailService emailService, ConversionService conversionService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.conversionService = conversionService;
    }

    @Override
    public void saveFromApiSource(UserCreateDTO userCreateDTO) {

        User toRegister = updateUserParamsFromUserCreateDTO(
                new User(UUID.randomUUID()),
                userCreateDTO,
                UserRole.ADMIN,
                UserStatus.WAITING_ACTIVATION);

        // TODO  ADD EXCEPTION HANDLING
        try {
            // TODO CHANGE EXCEPTION HANDLING DEPENDING ON CONSTRAINTS
            userRepository.save(toRegister);
        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


    }


    @Override
    public Integer saveFromUserSouce(UserRegistrationDTO userRegistrationDTO) {


        String mail = userRegistrationDTO.getMail();
        String fio = userRegistrationDTO.getFio();
        String password = userRegistrationDTO.getPassword();
        StructuredException structuredException = new StructuredException();

        //TODO CHANGE EXCEPTION HANDLING
        if (null == mail || mail.isBlank()) {
            structuredException.put(
                    "mail", "Почта не должна быть пустой"
            );
        }
        if (null == fio || fio.isBlank()) {
            structuredException.put(
                    "fio", "Фио не должно быть пустым"
            );
        }
        if (null == password || password.isBlank()) {

            structuredException.put(
                    "password", "Пароль не должен быть пустым"
            );
        }

        if (structuredException.hasExceptions()) {
            throw structuredException;
        }


        User toRegister = new User(UUID.randomUUID());


        updateUserParamsWithPassedArguments(mail, fio, password, toRegister);

        try {

            // TODO CHANGE EXCEPTION HANDLING DEPENDING ON CONSTRAINTS
            User user = userRepository.save(toRegister);
        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

        // TODO CHANGE EXCEPTION HANDLING
        try {

            Integer verificationCode = ThreadLocalRandom.current().nextInt(10000);
            codeHolder.put(mail, verificationCode);
            emailService.sendVerificationCodeMessage(mail, verificationCode);
            return verificationCode;

        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_SEND_VERIFICATION_EMAIL_EXCEPTION, e);
        }



    }

    @Override
    public void verifyUser(String email, Integer verificationCode) {

        StructuredException exception = new StructuredException();
        if (null == email || email.isBlank()) {
            exception.put("mail", "Значение почты не должно быть пустым");
        }
        if (null == verificationCode || verificationCode < 0) {
            exception.put("code","Значение кода подтверждения не должно быть пустым или меньше нуля");
        }
        if (exception.hasExceptions()) {
            throw exception;
        }
        Integer savedCode = this.codeHolder.get(email);
        if (savedCode == null) {
            throw new StructuredException(
                    "mail", "Не найден пользователь с такой почтой или код верификации истек"
            );
        }
        if (!savedCode.equals(verificationCode)) {
            throw new StructuredException(
                    "code", "Введен неверный код верификации"
            );

        }
        int res;

        try {
            res = userRepository.setUserActiveByEmail(email);
        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

        if (res != 1) {
            throw new GeneralException("Что-то пошло не так с активацией пользователя", new RuntimeException());
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
