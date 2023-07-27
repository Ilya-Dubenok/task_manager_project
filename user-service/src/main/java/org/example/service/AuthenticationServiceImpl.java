package org.example.service;

import jakarta.validation.Valid;
import org.example.config.property.ApplicationProperties;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.example.dao.api.IVerificationInfoRepository;
import org.example.dao.entities.verification.EmailStatus;
import org.example.dao.entities.verification.VerificationInfo;
import org.example.service.api.IAuthenticationService;
import org.example.service.api.ISenderInfoService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Validated
@Service
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final IUserService userService;


    private final IVerificationInfoRepository verificationInfoRepository;

    private final ConversionService conversionService;

    private final ISenderInfoService senderInfoService;

    private final ApplicationProperties properties;

    private final URI DEFAULT_REPLY_TO_URL;

    private final String DEFAULT_VERIFICATION_CODE_TEXT_FORMAT =
            "Добрый день! Для завершения регистрации перейдите по ссылке ниже\n" +
                    "http://%s/user_service/api/v1/users/verification?code=%s&mail=%s";

    private static final String DEFAULT_VERIFICATION_SUBJECT = "Подтверждение регистрации в приложении TaskManager";


    public AuthenticationServiceImpl(IUserService userService,
                                     IVerificationInfoRepository verificationInfoRepository,
                                     ConversionService conversionService,
                                     ISenderInfoService senderInfoService,
                                     ApplicationProperties properties) {
        this.userService = userService;
        this.verificationInfoRepository = verificationInfoRepository;
        this.conversionService = conversionService;
        this.senderInfoService = senderInfoService;
        this.properties = properties;
        this.DEFAULT_REPLY_TO_URL = URI.create("http://localhost/user_service/api/v1/users/notification");

    }

    @Override
    public Integer registerUser(@Valid UserRegistrationDTO userRegistrationDTO) {

        String mail = userRegistrationDTO.getMail();


        UserCreateDTO dto = conversionService.convert(userRegistrationDTO, UserCreateDTO.class);

        Integer verificationCode;

        userService.save(dto);
        try {
            verificationInfoRepository.cleanOldCodes(LocalDateTime.now(), 10);

            verificationCode = ThreadLocalRandom.current().nextInt(100000);
            VerificationInfo info = formVerificationInfo(mail, verificationCode);

            verificationInfoRepository.save(info);

        } catch (Exception e) {
            StructuredException structuredException = new StructuredException();

            if (DatabaseExceptionsMapper.isExceptionCauseRecognized(e, structuredException)) {
                throw structuredException;
            }
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);
        }

        //TODO REPLACE
        String message = "SOME DUM MESSAGE";

        String subject = "DUM SUBJECT";

        senderInfoService.sendEmailAssignmentWithReply(mail, subject,  message, DEFAULT_REPLY_TO_URL);

            return verificationCode;

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
