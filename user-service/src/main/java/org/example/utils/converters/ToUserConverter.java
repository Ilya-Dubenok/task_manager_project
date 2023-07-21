package org.example.utils.converters;

import org.example.core.dto.UserCreateDTO;
import org.example.dao.entities.user.User;
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
                new ConvertiblePair(UserCreateDTO.class, User.class)
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

        return null;

    }
}
