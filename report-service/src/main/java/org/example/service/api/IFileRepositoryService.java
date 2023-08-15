package org.example.service.api;

import org.example.dao.entities.ReportType;

public interface IFileRepositoryService {

    void saveFile(String fileName, ReportType reportType);



}
