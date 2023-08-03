package org.example.utils.converters;

import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserRole;
import org.example.dao.entities.user.UserStatus;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ToUserConverter implements
        GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(UserCreateDTO.class, User.class),
                new ConvertiblePair(UserRegistrationDTO.class, User.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        if (sourceType.getType().equals(UserCreateDTO.class)) {
            User res = new User();
            UserCreateDTO userCreateDTO = (UserCreateDTO) source;
            res.setPassword(userCreateDTO.getPassword());
            res.setMail(userCreateDTO.getMail());
            res.setRole(userCreateDTO.getRole());
            res.setStatus(userCreateDTO.getStatus());
            res.setFio(userCreateDTO.getFio());

            return res;
        }

        if (sourceType.getType().equals(UserRegistrationDTO.class)){

            User res = new User();
            UserRegistrationDTO userRegistrationDTO = (UserRegistrationDTO) source;

            res.setFio(userRegistrationDTO.getFio());
            res.setPassword(userRegistrationDTO.getPassword());
            res.setMail(userRegistrationDTO.getMail());


            return res;
        }

        return null;

    }
}
