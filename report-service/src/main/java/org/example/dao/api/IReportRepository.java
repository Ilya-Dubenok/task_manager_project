package org.example.dao.api;

import org.example.dao.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface IReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {


}
