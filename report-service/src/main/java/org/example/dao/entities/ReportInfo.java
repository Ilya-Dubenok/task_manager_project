package org.example.dao.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "report_info")
public class ReportInfo {

    @Id
    @Column(name = "report_uuid")
    private UUID reportUuid;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "bucket_name")
    private String bucketName;


    public ReportInfo() {
    }

    public ReportInfo(UUID reportUuid, String fileName, String bucketName) {
        this.reportUuid = reportUuid;
        this.fileName = fileName;
        this.bucketName = bucketName;
    }

    public UUID getReportUuid() {
        return reportUuid;
    }

    public void setReportUuid(UUID reportUuid) {
        this.reportUuid = reportUuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
