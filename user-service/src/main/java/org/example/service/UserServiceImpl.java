package org.example.service;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserRegistrationDTO;
import org.example.core.dto.utils.UserEntityToDTOConverter;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.IEmailService;
import org.example.service.api.IUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements IUserService {


    private IUserRepository userRepository;

    private IEmailService emailService;

    private Map<String, Integer> codeHolder = ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(5, TimeUnit.MINUTES)
            .build();

    public UserServiceImpl(IUserRepository userRepository, IEmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public boolean saveFromApiSource(UserCreateDTO userCreateDTO) {

        User toRegister = updateUserParamsFromUserCreateDTO(
                new User(UUID.randomUUID()),
                userCreateDTO,
                UserRole.ADMIN,
                UserStatus.WAITING_ACTIVATION);

        // TODO  ADD EXCEPTION HANDLING

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


        // TODO CHANGE EXCEPTION HANDLING
        toRegister.setMail(mail);
        toRegister.setPassword(password);
        toRegister.setFio(fio);
        toRegister.setRole(UserRole.USER);
        toRegister.setStatus(UserStatus.WAITING_ACTIVATION);

        User user = userRepository.save(toRegister);

        // TODO CHANGE EXCEPTION HANDLING
        try {

            Integer verificationCode = ThreadLocalRandom.current().nextInt(10000);
            codeHolder.put(mail, verificationCode);
            emailService.sendVerificationCodeMessage(mail, verificationCode);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправке кода аутентификации. Проверьте правильность почты или обратитесь позднее");
        }

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
                // TODO CHANGE EXCEPTION HANDLING
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
            // TODO ADD EXCEPTION HANDLING
            if (rowsPerPage < 1) {
                // TODO ADD EXCEPTION HANDLING
            }
            throw new RuntimeException("ПЕРЕДЕЛАТЬ");
        }


        Window<User> userWindow = userRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));
        Long count = userRepository.count();
        PageOfUserDTO res = UserEntityToDTOConverter.convertWindofOfUsersToPageOfUserDTO(
                userWindow, count, currentRequestedPage, rowsPerPage
        );

        // TODO ADD EXCEPTION HANDLING


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
