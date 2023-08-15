package org.example.dao;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.example.config.properties.ApplicationProperties;
import org.example.core.exception.GeneralException;
import org.example.dao.api.IFileRepository;
import org.example.core.exception.ObjectNotPresentException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FileRepositoryImpl implements IFileRepository {

    private final String URL_PREFIX = "http://";

    private final ApplicationProperties applicationProperties;

    private MinioClient minioClient;

    public FileRepositoryImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void saveFile(String fileName, String fileType) {

        boolean bucketExists = false;

        try {


            String bucketName = fileType.toLowerCase().replaceAll("_","");

            bucketExists = minioClient.bucketExists(
                    BucketExistsArgs
                            .builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists){

                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucketName)
                                .build());

            }

            minioClient.uploadObject(
                    UploadObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .filename(fileName)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }




    }

    @Override
    public String getFileUrl(String fileName, String fileType) {

        try {

            String bucketName = fileType.toLowerCase().replaceAll("_", "");

            minioClient.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());


            String url =
                    minioClient.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucketName)
                                    .object(fileName)
                                    .expiry(5, TimeUnit.MINUTES)
                                    .build());

            return url;

        } catch (ErrorResponseException e) {

            if (e.errorResponse().code().equals("NoSuchKey")) {
                //TODO place for logging

                throw new ObjectNotPresentException("Такого отчета не найдено в репозитории");

            } else {
                throw new GeneralException("Неизвестная ошибка в ходе выполнения операции");
            }
        } catch (Exception e) {
            throw new GeneralException("Неизвестная ошибка в ходе выполнения операции");
        }
    }


    @PostConstruct
    private void init() {

        ApplicationProperties.NetworkProp.Minio minio = applicationProperties.getNetwork().getMinio();

        String miniEndpoint = URL_PREFIX + minio.getHost();

        String accessKey = minio.getAccessKey();

        String secretKey = minio.getSecretKey();

        minioClient =
                MinioClient.builder()
                        .endpoint(miniEndpoint)
                        .credentials(accessKey, secretKey)
                        .build();

    }

}
