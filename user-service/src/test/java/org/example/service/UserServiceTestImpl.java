package org.example.service;

import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.ISenderInfoService;
import org.example.service.utils.JsonAuditMessagesFormer;
import org.example.utils.jwt.JwtTokenHandler;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service("testWithoutSecurityContext")
public class UserServiceTestImpl extends UserServiceImpl{
    public UserServiceTestImpl(
            IUserRepository userRepository,
            ConversionService conversionService,
            ISenderInfoService senderInfoService,
            PasswordEncoder passwordEncoder,
            UserHolder userHolder,
            JwtTokenHandler jwtTokenHandler,
            JsonAuditMessagesFormer auditMessagesFormer
    ) {
        super(userRepository, conversionService, senderInfoService, passwordEncoder, userHolder, jwtTokenHandler, auditMessagesFormer);
    }

    @Override
    public User getUserFromCurrentSecurityContext() {
        return new User(
                UUID.randomUUID(),
                "some_mail",
                "some_fio",
                UserRole.ADMIN,
                UserStatus.ACTIVATED,
                "12345"
        );
    }
}
