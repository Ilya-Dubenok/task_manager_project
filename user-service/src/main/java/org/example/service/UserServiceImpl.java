package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.audit.Type;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserLoginDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.core.exception.GeneralException;
import org.example.core.exception.StructuredException;
import org.example.core.exception.utils.DatabaseExceptionsMapper;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.example.service.api.ISenderInfoService;
import org.example.service.api.IUserService;
import org.example.utils.jwt.JwtTokenHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Validated
@Service
@Primary
public class UserServiceImpl implements IUserService {


    private final IUserRepository userRepository;


    private final ConversionService conversionService;

    private final ISenderInfoService senderInfoService;

    private final PasswordEncoder passwordEncoder;

    private final UserHolder userHolder;

    private final JwtTokenHandler jwtTokenHandler;


    public UserServiceImpl(IUserRepository userRepository,
                           ConversionService conversionService,
                           ISenderInfoService senderInfoService,
                           PasswordEncoder passwordEncoder,
                           UserHolder userHolder, JwtTokenHandler jwtTokenHandler) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
        this.senderInfoService = senderInfoService;
        this.passwordEncoder = passwordEncoder;
        this.userHolder = userHolder;
        this.jwtTokenHandler = jwtTokenHandler;
    }

    @Override
    @Transactional
    public void save(@Valid UserCreateDTO userCreateDTO) {

        encryptUserCreateDTOPassword(userCreateDTO);

        User toSave = conversionService.convert(
                userCreateDTO, User.class
        );
        toSave.setUuid(UUID.randomUUID());

        User save = userRepository.save(toSave);


        try {

            senderInfoService.sendAudit(
                    getUserFromCurrentSecurityContext()
                    , ISenderInfoService.AuditMessages.USER_CREATED_MESSAGE, Type.USER, save.getUuid().toString());
        } catch (UsernameNotFoundException ignored) {

        }


    }

    @Override
    @Transactional
    public void save(@Valid UserRegistrationDTO userRegistrationDTO) {
        User toSave = conversionService.convert(userRegistrationDTO, User.class);
        toSave.setPassword(
                passwordEncoder.encode(
                        userRegistrationDTO.getPassword()
                )
        );
        toSave.setRole(UserRole.USER);
        toSave.setStatus(UserStatus.WAITING_ACTIVATION);
        toSave.setUuid(UUID.randomUUID());

        userRepository.save(toSave);


    }

    @Override
    public User getUserFromCurrentSecurityContext() {
        return getByUUID(
                UUID.fromString(userHolder.getUser().getUsername())
        );
    }


    @Override
    @Transactional(readOnly = true)
    public User getByUUID(UUID uuid) {
        return userRepository.findById(
                uuid
        ).orElseThrow(
                () -> new GeneralException("Не найден пользователь по такому uuid")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getList(List<UUID> uuids) {

        if (uuids == null || uuids.contains(null)) {
            throw new GeneralException("Переданы неверные данные для поиска");
        }

        try {

            return userRepository.findAllById(uuids);

        } catch (Exception e) {
            throw new GeneralException("Произошла ошибка при поиске");
        }


    }

    @Override
    @Transactional
    public void update(UUID uuid, LocalDateTime dt_update, @Valid UserCreateDTO userCreateDTO) {

        User toUpdate = userRepository.findById(uuid).orElseThrow(
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

        User copyBeforeSaving = copyUserBeforeSupposedChanges(toUpdate);

        toUpdate = updateUserParamsFromUserCreateDTO(toUpdate, userCreateDTO);


        toUpdate = userRepository.save(toUpdate);

        if (!toUpdate.equals(copyBeforeSaving)) {


            senderInfoService.sendAudit(
                    getUserFromCurrentSecurityContext()
                    , ISenderInfoService.AuditMessages.USER_UPDATED_MESSAGE, Type.USER,
                    toUpdate.getUuid().toString());
        }


    }


    @Override
    @Transactional(readOnly = true)
    public Page<User> getPage(Integer currentRequestedPage, Integer rowsPerPage) {

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


        Page<User> page = userRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));

        return page;


    }

    @Override
    @Transactional
    public int setUserActiveByEmail(String email) {

            int i = userRepository.setUserActiveByEmail(email);

            User user = userRepository.findByMailAndStatusEquals(email, UserStatus.ACTIVATED);

            if (user != null && i != 0) {

                senderInfoService.sendAudit(
                        user, ISenderInfoService.AuditMessages.USER_REGISTERED_MESSAGE, Type.USER, user.getUuid().toString()
                );
            }

            return i;



    }

    @Override
    @Transactional(readOnly = true)
    public User login(@Valid UserLoginDTO userLoginDTO) {

        String mail = userLoginDTO.getMail();

        User user;

        try {

            user = userRepository.findByMail(mail);

        } catch (Exception e) {
            throw new GeneralException(GeneralException.DEFAULT_DATABASE_EXCEPTION_MESSAGE, e);

        }

        if (user == null) {
            throw new StructuredException("mail", "Пользователь не найден");
        }

        if (!passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword())) {
            throw new StructuredException("password", "Пароль неверный");
        }

        return user;

    }

    @Override
    @Transactional(readOnly = true)
    public String loginAndReceiveToken(@Valid UserLoginDTO userLoginDTO) {

        User find = login(userLoginDTO);

        String uuid = find.getUuid().toString();

        return jwtTokenHandler.generateAccessToken(uuid);


    }

    private void encryptUserCreateDTOPassword(UserCreateDTO userCreateDTO) {
        userCreateDTO.setPassword(
                passwordEncoder.encode(userCreateDTO.getPassword())
        );

    }


    private User updateUserParamsFromUserCreateDTO(User user, UserCreateDTO userCreateDTO) {

        encryptUserCreateDTOPassword(userCreateDTO);

        user.setMail(userCreateDTO.getMail());
        user.setFio(userCreateDTO.getFio());

        user.setRole(userCreateDTO.getRole());

        user.setStatus(userCreateDTO.getStatus());

        user.setPassword(userCreateDTO.getPassword());

        return user;

    }



    private User copyUserBeforeSupposedChanges(User toUpdate) {
        User copyBeforeSaving = new User(
                toUpdate.getUuid(),
                toUpdate.getMail(),
                toUpdate.getFio(),
                toUpdate.getRole(),
                toUpdate.getStatus(),
                toUpdate.getPassword()
        );

        copyBeforeSaving.setDtCreate(toUpdate.getDtUpdate());
        copyBeforeSaving.setDtUpdate(toUpdate.getDtUpdate());
        return copyBeforeSaving;
    }
}
