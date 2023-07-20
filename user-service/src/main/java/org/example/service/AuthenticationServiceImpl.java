package org.example.service;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.IAuthenticationService;
import org.example.service.api.IEmailService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private IUserService userService;

    private IEmailService emailService;

    private ConversionService conversionService;


    private Map<String, Integer> codeHolder = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(5, TimeUnit.MINUTES)
            .build();


    public AuthenticationServiceImpl(IUserService userService, IEmailService emailService, ConversionService conversionService) {
        this.userService = userService;
        this.emailService = emailService;
        this.conversionService = conversionService;
    }

    @Override
    public Integer registerUser(UserRegistrationDTO userRegistrationDTO) {

        String mail = userRegistrationDTO.getMail();
        String fio = userRegistrationDTO.getFio();
        String password = userRegistrationDTO.getPassword();
        StructuredException structuredException = new StructuredException();

        //TODO CHANGE EXCEPTION HANDLING?
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


        //TODO ADD CONVERTER
        UserCreateDTO dto = new UserCreateDTO();

        dto.setFio(fio);
        dto.setPassword(password);
        dto.setMail(mail);
        dto.setRole(UserRole.USER);
        dto.setStatus(UserStatus.WAITING_ACTIVATION);

        try {

            // TODO CHANGE EXCEPTION HANDLING DEPENDING ON CONSTRAINTS
            userService.save(dto);
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
    public void verifyUserWithCode(Integer verificationCode, String email) {

        StructuredException exception = new StructuredException();
        if (null == email || email.isBlank()) {
            exception.put("mail", "Значение почты не должно быть пустым");
        }
        if (null == verificationCode || verificationCode < 0) {
            exception.put("code", "Значение кода подтверждения не должно быть пустым или меньше нуля");
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
            res = userService.setUserActiveByEmail(email);
        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

        if (res != 1) {
            throw new GeneralException("Что-то пошло не так с активацией пользователя", new RuntimeException());
        }

    }


}
