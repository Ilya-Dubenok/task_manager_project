package org.example.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import jakarta.annotation.PostConstruct;
import org.example.config.properties.ApplicationProperties;
import org.example.dao.entities.ReportType;
import org.example.service.api.IFileRepositoryService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class FileRepositoryServiceImpl implements IFileRepositoryService {

    private final String URL_PREFIX = "http://";

    private final ApplicationProperties applicationProperties;

    private MinioClient minioClient;

    public FileRepositoryServiceImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void saveFile(String fileName, ReportType reportType) {

        boolean bucketExists = false;

        try {


            String bucketName = reportType.toString().toLowerCase().replaceAll("_","");

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
                            .object(ThreadLocalRandom.current().nextInt(0,10)+fileName)
                            .filename(fileName)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }




    }


    @PostConstruct
    private void init() {

        ApplicationProperties.NetworkProp.Minio minio = applicationProperties.getNetwork().getMinio();

        minio.setSecretKey("new secret key");

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
