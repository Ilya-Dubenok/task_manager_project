package org.example.utils.converters;

import org.example.core.dto.GenerizedDTO;
import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserDTO;
import org.example.dao.entities.user.User;
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
                new ConvertiblePair(User.class, GenerizedDTO.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType().equals(PageImpl.class)) {
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


        } else if (sourceType.getType().equals(User.class) && targetType.getType().equals(UserDTO.class)) {
            UserDTO res = new UserDTO();
            User user = (User) source;
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
        } else if (
                sourceType.getType().equals(User.class)
                        && targetType.getType().equals(GenerizedDTO.class)
                        && targetType.getResolvableType().getGeneric(0).getType().equals(User.class)
        ) {

            GenerizedDTO<User> res = new GenerizedDTO<>();
            res.setNumberOfSth(10);
            res.setField((User) source);
            res.setContent(
                    List.of((User) source, (User) source, (User) source, (User) source)
            );

            return res;

        }


        return null;

    }


}