package org.example.service;

import jakarta.validation.Valid;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.dao.entities.project.Project;
import org.example.service.api.IProjectService;
import org.example.service.api.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
@Service
public class ProjectServiceImpl implements IProjectService {

    private IUserService userService;

    public ProjectServiceImpl(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void save(@Valid ProjectCreateDTO projectCreateDTO) {

    }

    @Override
    public Project findByUUID(UUID uuid) {
        return null;
    }

    @Override
    public Page<Project> getPage(Integer currentRequestedPage, Integer rowsPerPage) {
        return null;
    }
}
