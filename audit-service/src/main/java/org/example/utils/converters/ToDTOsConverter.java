package org.example.utils.converters;

import org.example.core.dto.AuditDTO;
import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.UserDTO;
import org.example.dao.entities.audit.Audit;
import org.example.dao.entities.audit.Type;
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
public class ToDTOsConverter<IN, OUT> implements
        GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(Page.class, PageOfTypeDTO.class),
                new ConvertiblePair(User.class, UserDTO.class),
                new ConvertiblePair(Audit.class, AuditDTO.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType().equals(PageImpl.class)
                && targetType.getType().equals(PageOfTypeDTO.class)
                && targetType.getResolvableType().getGeneric(0).getType().equals(AuditDTO.class)
        ) {

            PageOfTypeDTO<AuditDTO> res = new PageOfTypeDTO<>();
            Page<Audit> info = (Page<Audit>) source;
            res.setNumber(info.getNumber());
            res.setTotalPages(info.getTotalPages());
            res.setTotalElements(info.getTotalElements());
            res.setFirst(info.isFirst());
            res.setLast(!info.hasNext());
            List<AuditDTO> content = new ArrayList<>();
            for (Audit audit : info.toList()) {
                content.add(
                        (AuditDTO) this.convert(audit, TypeDescriptor.valueOf(Audit.class), TypeDescriptor.valueOf(AuditDTO.class))
                );
            }
            res.setSize(info.getSize());
            res.setNumberOfElements(content.size());
            res.setContent(content);

            return res;


        }

        if (sourceType.getType().equals(User.class) && targetType.getType().equals(UserDTO.class)) {
            UserDTO res = new UserDTO();
            User user = (User) source;
            res.setUuid(user.getUuid());
            res.setMail(user.getMail());
            res.setFio(user.getFio());
            res.setRole(user.getRole());
            return res;
        }

        if (sourceType.getType().equals(Audit.class) && targetType.getType().equals(AuditDTO.class)) {
            AuditDTO res = new AuditDTO();
            Audit audit = (Audit) source;

            res.setUuid(audit.getUuid());
            res.setDtCreate(
                    ZonedDateTime.of(audit.getDtCreate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setText(audit.getText());
            Type type = audit.getType();
            res.setType(type);
            res.setId(type.getId());
            res.setUser(
                    (UserDTO) this.convert(audit.getUser(),TypeDescriptor.valueOf(User.class), TypeDescriptor.valueOf(UserDTO.class))
            );

            return res;
        }

        


        return null;

    }


}
