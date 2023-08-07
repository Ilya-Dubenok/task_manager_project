package org.example.utils.converters;

import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.user.User;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ToEntityConverter implements
        GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(UserDTO.class, User.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        Class<?> expectedSourceClass = sourceType.getType();

        Class<?> expectedTargetClass = targetType.getType();

        if (expectedSourceClass.equals(UserDTO.class) && expectedTargetClass.equals(User.class)) {

            User res = new User();
            UserDTO userDTO = (UserDTO) source;
            res.setUuid(userDTO.getUuid());
            return res;

        }

        return null;
    }
}
