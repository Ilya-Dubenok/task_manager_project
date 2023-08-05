package org.example.utils.converters;

import org.example.core.dto.audit.AuditDTO;
import org.example.core.dto.audit.PageOfTypeDTO;
import org.example.dao.entities.audit.Audit;
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



        if (sourceType.getType().equals(Audit.class) && targetType.getType().equals(AuditDTO.class)) {
            AuditDTO res = new AuditDTO();
            Audit audit = (Audit) source;

            res.setUuid(audit.getUuid());
            res.setDtCreate(
                    ZonedDateTime.of(audit.getDtCreate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setText(audit.getText());
            res.setType(audit.getType());
            res.setId(audit.getId());
            res.setUser(audit.getUser());

            return res;
        }

        


        return null;

    }


}
