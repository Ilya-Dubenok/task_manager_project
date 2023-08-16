package org.example.utils.converters;

import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.report.Params;
import org.example.core.dto.report.ReportDTO;
import org.example.core.dto.report.ReportParamAudit;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportType;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
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
                new ConvertiblePair(Map.class, ReportParamAudit.class),
                new ConvertiblePair(Page.class, PageOfTypeDTO.class)

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

        if (expectedSourceClass.equals(PageImpl.class) && expectedTargetClass.equals(PageOfTypeDTO.class)) {

            Type resolvedGenericType = targetType.getResolvableType().getGeneric(0).getType();

            if (resolvedGenericType.equals(ReportDTO.class)) {

                PageOfTypeDTO<ReportDTO> res = new PageOfTypeDTO<>();

                Page<Report> reportPage = (Page<Report>) source;

                List<ReportDTO> content = new ArrayList<>();

                for (Report report : reportPage.toList()) {
                    content.add(
                            (ReportDTO) this.convert(report,
                                    TypeDescriptor.valueOf(Report.class),
                                    TypeDescriptor.valueOf(ReportDTO.class))
                    );

                }

                fillPageWithValues(res, reportPage, content);

                return res;

            }


        }

        return null;

    }


    private <T, E> void fillPageWithValues(PageOfTypeDTO<T> res, Page<E> source, List<T> content) {

        res.setNumber(source.getNumber());
        res.setTotalPages(source.getTotalPages());
        res.setTotalElements(source.getTotalElements());
        res.setFirst(source.isFirst());
        res.setLast(!source.hasNext());
        res.setSize(source.getSize());
        res.setNumberOfElements(content.size());
        res.setContent(content);

    }



}
