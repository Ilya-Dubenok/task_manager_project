package org.example.dao.api;

import org.example.dao.entities.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IProjectRepository extends JpaRepository<Project, UUID> {

}
