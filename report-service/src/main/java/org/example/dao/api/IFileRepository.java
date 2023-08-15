package org.example.dao.api;

public interface IFileRepository {

    void saveFile(String fileName, String fileType);

    String getFileUrl(String fileName, String fileType);





}
