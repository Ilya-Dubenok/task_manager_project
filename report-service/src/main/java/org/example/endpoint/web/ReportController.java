package org.example.endpoint.web;


import jakarta.servlet.http.HttpServletResponse;
import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.report.ReportDTO;
import org.example.dao.entities.Report;
import org.example.dao.entities.ReportType;
import org.example.service.api.IReportService;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/report")
public class ReportController {

    private IReportService reportService;

    private ConversionService conversionService;

    public ReportController(IReportService reportService, ConversionService conversionService) {
        this.reportService = reportService;
        this.conversionService = conversionService;
    }


    @PostMapping(value = "/{type}")
    public ResponseEntity<?> postReport(@RequestBody Map<String, String> requestParams, @PathVariable ReportType type) {

        reportService.putReportRequest(requestParams, type);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<?> getPage(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                     @RequestParam(value = "size", defaultValue = "20") Integer size) {


        Page<Report> pageOfReports = reportService.getPageOfReports(page, size);

        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                PageOfTypeDTO.class, ReportDTO.class
        );

        Object converted = conversionService.convert(
                pageOfReports, TypeDescriptor.valueOf(PageImpl.class),
                new TypeDescriptor(resolvableType, null, null)
        );

        return new ResponseEntity<>(converted, HttpStatus.OK);

    }

    @GetMapping(value = "/{uuid}/export")
    public void getReportFile(@PathVariable UUID uuid, HttpServletResponse response) throws IOException {

        String url = reportService.getReportFileUrl(uuid);

        response.sendRedirect(url);

    }


    @RequestMapping(method = RequestMethod.HEAD, value = "/{uuid}/export")
    public ResponseEntity<?> checkIfReady(@PathVariable UUID uuid) throws IOException {

        boolean reportAvailable = reportService.isReportAvailable(uuid);

        if (reportAvailable) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

    }




}
