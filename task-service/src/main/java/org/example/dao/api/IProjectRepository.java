package org.example.dao.api;

import org.example.dao.entities.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface IProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {


    Page<Project> findAllByOrderByUuid(Pageable pageable);


}
