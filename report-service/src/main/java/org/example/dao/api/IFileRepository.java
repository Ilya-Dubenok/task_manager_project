package org.example.dao.api;

public interface IFileRepository {

    void saveFile(String fileName, String bucketName);

    String getFileUrl(String fileName, String bucketName);





}
