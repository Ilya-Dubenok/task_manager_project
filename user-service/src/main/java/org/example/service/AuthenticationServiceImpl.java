package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.example.dao.api.IVerificationInfoRepository;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.dao.entities.verification.EmailStatus;
import org.example.dao.entities.verification.VerificationInfo;
import org.example.service.api.IAuthenticationService;
import org.example.service.api.IEmailService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Validated
@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private IUserService userService;

    private IEmailService emailService;

    private IVerificationInfoRepository verificationInfoRepository;

    private ConversionService conversionService;


    public AuthenticationServiceImpl(IUserService userService, IEmailService emailService, IVerificationInfoRepository verificationInfoRepository, ConversionService conversionService) {
        this.userService = userService;
        this.emailService = emailService;
        this.verificationInfoRepository = verificationInfoRepository;
        this.conversionService = conversionService;
    }

    @Override
    public Integer registerUser(@Valid UserRegistrationDTO userRegistrationDTO) {

        String mail = userRegistrationDTO.getMail();
        String fio = userRegistrationDTO.getFio();
        String password = userRegistrationDTO.getPassword();
        StructuredException structuredException = new StructuredException();



        //TODO ADD CONVERTER
        UserCreateDTO dto = new UserCreateDTO();

        dto.setFio(fio);
        dto.setPassword(password);
        dto.setMail(mail);
        dto.setRole(UserRole.USER);
        dto.setStatus(UserStatus.WAITING_ACTIVATION);

        Integer verificationCode;

        userService.save(dto);
        try {
            verificationInfoRepository.cleanOldCodes(LocalDateTime.now(), 10);

            verificationCode = ThreadLocalRandom.current().nextInt(100000);
            VerificationInfo info = formVerificationInfo(mail, verificationCode);

            verificationInfoRepository.save(info);

        } catch (Exception e) {
            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


        // TODO CHANGE EXCEPTION HANDLING
        try {

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
        verificationInfoRepository.cleanOldCodes(LocalDateTime.now(), 10);
        VerificationInfo verificationInfo = verificationInfoRepository.findByMail(email);
        if (verificationInfo == null) {

            throw new StructuredException(
                    "mail", "Не найден пользователь с такой почтой или код верификации истек"
            );
        }
        Integer savedCode = verificationInfo.getCode();

        if (!savedCode.equals(verificationCode)) {
            throw new StructuredException(
                    "code", "Введен неверный код верификации"
            );

        }

        userService.setUserActiveByEmail(email);
        try {
            verificationInfoRepository.cleanUsedCode(email);

        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }


    }

    private static VerificationInfo formVerificationInfo(String mail, Integer verificationCode) {
        VerificationInfo info = new VerificationInfo();
        info.setUuid(UUID.randomUUID());
        info.setMail(mail);
        info.setCode(verificationCode);
        info.setRegisteredTime(LocalDateTime.now());
        info.setEmailStatus(EmailStatus.WAITING_TO_BE_SENT);
        info.setCountOfAttempts(1);

        return info;
    }


}
