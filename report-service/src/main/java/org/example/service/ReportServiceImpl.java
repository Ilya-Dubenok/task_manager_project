package org.example.service;

import org.example.core.dto.report.ReportParamAudit;
import org.example.core.exception.GeneralException;
import org.example.core.exception.ObjectNotPresentException;
import org.example.core.exception.StructuredException;
import org.example.dao.api.IFileRepository;
import org.example.dao.api.IReportInfoRepository;
import org.example.dao.api.IReportRepository;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportInfo;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.example.service.api.IReportService;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportServiceImpl implements IReportService {

    private ConversionService conversionService;

    private IReportRepository reportRepository;

    private IFileRepository fileRepository;

    private IReportInfoRepository reportInfoRepository;


    public ReportServiceImpl(IReportRepository reportRepository, ConversionService conversionService, IFileRepository fileRepository, IReportInfoRepository reportInfoRepository) {
        this.reportRepository = reportRepository;
        this.conversionService = conversionService;
        this.fileRepository = fileRepository;
        this.reportInfoRepository = reportInfoRepository;
    }

    @Override
    @Transactional
    public void putReportRequest(Map<String, String> stringParams, ReportType reportType) {

        ResolvableType type = ResolvableType.forClassWithGenerics(Map.class, ReportParamAudit.class, null);

        Map<?, ?> params;

        try {
            params = (Map<?, ?>) conversionService.convert(stringParams,
                    TypeDescriptor.valueOf(Map.class),
                    new TypeDescriptor(type, null, null)
            );
        } catch (ConversionFailedException e) {

            Throwable cause = e.getCause();
            if (cause instanceof StructuredException) {
                throw (StructuredException) cause;
            } else {
                throw new GeneralException("Произошла неизвестна ошибка при обработке данных");
            }
        }


        LocalDate from = (LocalDate) params.get("from");
        LocalDate to = (LocalDate) params.get("to");

        if (from.isAfter(to)) {
            throw new GeneralException("to must be AFTER false");
        }

        Report report = new Report();
        report.setParams((Map<String, Object>) params);
        report.setUuid(UUID.randomUUID());

        report.setType(reportType);
        String reportDescription = reportType.getReportDescription(stringParams);
        report.setDescription(reportDescription);
        report.setStatus(ReportStatus.LOADED);


        try {
            reportRepository.save(report);
        } catch (Exception e) {

            throw new GeneralException("Произошла ошибка неизвестного характера");

        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<Report> getReportsWithTypeAndStatus(ReportType type, ReportStatus status) {
        return reportRepository.findByTypeIsAndStatusIs(type, status);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isReportAvailable(UUID reportUuid) {
        return reportInfoRepository.existsById(reportUuid);
    }

    @Override
    @Transactional
    public void setStatusFailed(UUID reportUuid) {
        Report report = reportRepository.findById(reportUuid).orElseThrow(
                () -> new GeneralException("Unknown condition exception")
        );

        report.setStatus(ReportStatus.ERROR);

        reportRepository.saveAndFlush(report);

    }

    @Override
    @Transactional
    public void setStatus(UUID uuid, ReportStatus reportStatus) {

        reportRepository.updateStatus(uuid, reportStatus.toString());

    }

    @Override
    @Transactional(readOnly = true)
    public String getReportFileUrl(UUID uuid) {

        ReportInfo reportInfo = reportInfoRepository.findByReportUuid(uuid).orElseThrow(() -> new ObjectNotPresentException("Отчет не найден"));

        return fileRepository.getFileUrl(reportInfo.getFileName(), reportInfo.getBucketName());

    }

    @Override
    @Transactional
    public void saveReportInfo(UUID reportUuid, String fileName, String bucketName) {

        ReportInfo reportInfo = new ReportInfo();
        reportInfo.setReportUuid(reportUuid);
        reportInfo.setFileName(fileName);
        reportInfo.setBucketName(bucketName);

        reportInfoRepository.save(reportInfo);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> getPageOfReports(Integer page, Integer size) {

        validatePageArguments(page, size);

        return reportRepository.findAll(PageRequest.of(page, size, Sort.by("uuid")));

    }


    private static void validatePageArguments(Integer currentRequestedPage, Integer rowsPerPage) {
        StructuredException exception = new StructuredException();

        if (currentRequestedPage < 0) {

            exception.put("page", "Номер страницы не может быть меньше 0");

        }
        if (rowsPerPage < 1) {
            exception.put("size", "Размер страницы не может быть меньше 0");

        }

        if (exception.hasExceptions()) {
            throw exception;
        }
    }
}
