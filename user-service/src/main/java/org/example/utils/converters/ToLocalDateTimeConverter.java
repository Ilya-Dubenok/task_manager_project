package org.example.utils.converters;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TimeZone;

@Component
public class ToLocalDateTimeConverter<IN, OUT>
        implements GenericConverter {
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(
                new ConvertiblePair(String.class, LocalDateTime.class),
                new ConvertiblePair(Long.class, LocalDateTime.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (sourceType.getType().equals(String.class)) {
            long timeInMillis = Long.parseLong((String) source);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMillis),
                    TimeZone.getDefault().toZoneId());
        } else if (sourceType.getType().equals(Long.class)) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) source),
                    TimeZone.getDefault().toZoneId());
        }

        return null;


    }


}
