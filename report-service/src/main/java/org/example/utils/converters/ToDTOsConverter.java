package org.example.utils.converters;

import org.example.core.dto.report.Params;
import org.example.core.dto.report.ReportDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
public class ToDTOsConverter<IN, OUT> implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Set.of(

                new ConvertiblePair(Report.class, ReportDTO.class),
                new ConvertiblePair(Map.class, ReportParamAudit.class)
        );

    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        Class<?> expectedSourceClass = sourceType.getType();

        Class<?> expectedTargetClass = targetType.getType();

        if (expectedSourceClass.equals(Report.class) && expectedTargetClass.equals(ReportDTO.class)) {

            ReportDTO res = new ReportDTO();

            Report report = (Report) source;

            res.setUuid(report.getUuid());
            res.setDtCreate(
                    ZonedDateTime.of(report.getDtCreate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );
            res.setDtUpdate(
                    ZonedDateTime.of(report.getDtUpdate(), ZoneId.systemDefault()).toInstant().toEpochMilli()
            );

            res.setStatus(report.getStatus());

            ReportType type = report.getType();
            res.setType(type);

            res.setDescription(report.getDescription());

            Params converted = (Params) this.convert(report.getParams(), TypeDescriptor.valueOf(Map.class), TypeDescriptor.valueOf(type.getTargetClass()));

            res.setParams(converted);

            return res;

        }

        if (expectedSourceClass.equals(Map.class) && expectedTargetClass.equals(ReportParamAudit.class)) {

            ReportParamAudit res = new ReportParamAudit();

            Map<String,Object> params =  (Map<String, Object>) source;

            String user = (String) params.get("user");

            res.setUser(UUID.fromString(user));

            List<?> fromNums = (List<?>) params.get("from");

            LocalDate from = LocalDate.of((int) fromNums.get(0), (int) fromNums.get(1), (int) fromNums.get(2));

            List<?> toNums = (List<?>) params.get("to");

            LocalDate to = LocalDate.of((int) toNums.get(0), (int) toNums.get(1), (int) toNums.get(2));

            res.setFrom(from);

            res.setTo(to);


            return res;

        }


        return null;

    }



}
