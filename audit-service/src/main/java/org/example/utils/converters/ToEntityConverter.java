package org.example.utils.converters;

import org.example.core.dto.audit.AuditCreateDTO;
import org.example.dao.entities.audit.Audit;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class ToEntityConverter implements
        GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(AuditCreateDTO.class, Audit.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType().equals(AuditCreateDTO.class) && targetType.getType().equals(Audit.class)) {

            AuditCreateDTO auditCreateDTO = (AuditCreateDTO) source;

            Audit res = new Audit();
            res.setUuid(UUID.randomUUID());
            res.setType(auditCreateDTO.getType());
            res.setId(auditCreateDTO.getId());
            res.setUser(auditCreateDTO.getUser());
            res.setText(auditCreateDTO.getText());

            return res;


        }

        return null;
    }
}
