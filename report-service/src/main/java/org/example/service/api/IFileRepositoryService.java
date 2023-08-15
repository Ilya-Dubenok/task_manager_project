package org.example.service.api;

import org.example.dao.entities.ReportType;

import java.util.UUID;

public interface IFileRepositoryService {

    void saveFile(String fileName, String fileType);

    String getFileUrl(String fileName, String fileType);





}
