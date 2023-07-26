package org.example.utils.converters;

import org.example.core.dto.audit.AuditUserDTO;
import org.example.core.dto.user.PageOfUserDTO;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ToUserDTOsConverter<IN, OUT> implements
        GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(Page.class, PageOfUserDTO.class),
                new ConvertiblePair(User.class, UserDTO.class),
                new ConvertiblePair(User.class, AuditUserDTO.class),
                new ConvertiblePair(UserRegistrationDTO.class, UserCreateDTO.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Class<?> extractedSourceType = sourceType.getType();

        Class<?> extractedTargetType = targetType.getType();


        if (extractedSourceType.equals(PageImpl.class)) {
            PageOfUserDTO res = new PageOfUserDTO();
            Page<User> info = (Page<User>) source;
            res.setNumber(info.getNumber());
            res.setTotalPages(info.getTotalPages());
            res.setTotalElements(info.getTotalElements());
            res.setFirst(info.isFirst());
            res.setLast(!info.hasNext());
            List<UserDTO> content = new ArrayList<>();
            for (User user : info.toList()) {
                content.add(
                        (UserDTO) this.convert(user, TypeDescriptor.valueOf(User.class), TypeDescriptor.valueOf(UserDTO.class))
                );
            }
            res.setSize(info.getSize());
            res.setNumberOfElements(content.size());
            res.setContent(content);

            return res;


        }

        if (extractedSourceType.equals(User.class)) {

            User user = (User) source;


            if (extractedTargetType.equals(UserDTO.class)) {


                UserDTO res = new UserDTO();
                res.setUuid(user.getUuid());
                res.setStatus(user.getStatus());
                res.setRole(user.getRole());
                res.setFio(user.getFio());
                res.setMail(user.getMail());
                res.setDtCreate(
                        ZonedDateTime.of(user.getDtCreate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
                );
                res.setDtUpdate(
                        ZonedDateTime.of(user.getDtUpdate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
                );
                return res;
            }

            if (extractedTargetType.equals(AuditUserDTO.class)){

                AuditUserDTO res = new AuditUserDTO();
                res.setUuid(user.getUuid());
                res.setFio(user.getFio());
                res.setMail(user.getMail());
                res.setRole(user.getRole());

                return res;


            }

        }

        if (extractedSourceType.equals(UserRegistrationDTO.class)) {

            UserRegistrationDTO userRegistrationDTO = (UserRegistrationDTO) source;

            UserCreateDTO res = new UserCreateDTO();

            res.setFio(userRegistrationDTO.getFio());
            res.setPassword(userRegistrationDTO.getPassword());
            res.setMail(userRegistrationDTO.getMail());
            res.setRole(UserRole.USER);
            res.setStatus(UserStatus.WAITING_ACTIVATION);

            return res;

        }


        return null;

    }


}
