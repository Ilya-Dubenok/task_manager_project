package org.example.endpoint.web;


import org.example.dao.entities.ReportType;
import org.example.service.api.IReportService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

}
