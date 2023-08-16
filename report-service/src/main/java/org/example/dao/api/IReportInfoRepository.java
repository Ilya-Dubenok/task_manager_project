package org.example.dao.api;

import org.example.dao.entities.ReportInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IReportInfoRepository extends JpaRepository<ReportInfo, UUID> {


    Optional<ReportInfo> findByReportUuid(UUID reportUuid);


}
