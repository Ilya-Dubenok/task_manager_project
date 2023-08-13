package org.example.utils.converters;

import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.core.exception.StructuredException;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class ToEntityConverter implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(Map.class, Map.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        Class<?> expectedSourceClass = sourceType.getType();

        Class<?> expectedTargetClass = targetType.getType();


        if (expectedSourceClass.equals(Map.class) && expectedTargetClass.equals(Map.class)) {

            Type resolvedGenericType = targetType.getResolvableType().getGeneric(0).getType();

            if (resolvedGenericType.equals(ReportParamAudit.class)) {

                return getReportParamAuditMap((Map<String, String>) source);

            }


        }


        return null;
    }

    private static Map<String, Object> getReportParamAuditMap(Map<String, String> source) {
        Map<String, String> initParams = source;

        Map<String, Object> res = new HashMap<>();

        StructuredException e = new StructuredException();

        String userUuid = initParams.get("user");

        if (null == userUuid) {

            e.put("user", "must not be null in request");

        } else {

            try {
                res.put("user", UUID.fromString(userUuid));
            } catch (Exception exception) {

                e.put("user", "uuid is malformed");

            }

        }

        String toString = initParams.get("to");

        if (null == toString) {
            e.put("to", "must not be null in request");

        } else {

            try {
                res.put("to",LocalDate.parse(toString));

            } catch (Exception exception) {

                e.put("to", "to field is malformed");

            }

        }

        String fromString = initParams.get("from");

        if (null == fromString) {
            e.put("from", "must not be null in request");

        } else {

            try {
                
                res.put("from",LocalDate.parse(fromString));

            } catch (Exception exception) {

                e.put("from", "from field is malformed");
            }
        }

        if (e.hasExceptions()) {
            throw e;
        }

        return res;
    }
}
