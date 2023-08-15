package org.example.dao.api;

import org.example.dao.entities.Report;
import org.example.dao.entities.ReportStatus;
import org.example.dao.entities.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

    List<Report> findByTypeIsAndStatusIs(ReportType type, ReportStatus status);

    Optional<Report> findByUuidAndStatusIs(UUID uuid, ReportStatus status);

    @Query(value = "UPDATE report SET " +
            "status = ?2 WHERE uuid = ?1"
            ,nativeQuery = true)
    @Modifying(flushAutomatically = true)
    void updateStatus(UUID uuid, String status);

}
