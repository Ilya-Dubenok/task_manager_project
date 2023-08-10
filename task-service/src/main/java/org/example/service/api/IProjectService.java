package org.example.service.api;

import jakarta.validation.Valid;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.dao.entities.project.Project;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IProjectService {

    Project save(@Valid ProjectCreateDTO projectCreateDTO);

    Project update(UUID uuid, LocalDateTime dtUpdate, @Valid ProjectCreateDTO projectCreateDTO);

    Project findByUUID(UUID uuid);

    Page<Project> getPage(Integer currentRequestedPage, Integer rowsPerPage, Boolean showArchived);


}
