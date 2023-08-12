package org.example.endpoint.web;


import org.example.core.dto.PageOfTypeDTO;
import org.example.core.dto.project.ProjectCreateDTO;
import org.example.core.dto.project.ProjectDTO;
import org.example.dao.entities.project.Project;
import org.example.service.api.IProjectService;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final IProjectService projectService;

    private final ConversionService conversionService;


    public ProjectController(IProjectService projectService, ConversionService conversionService) {
        this.projectService = projectService;
        this.conversionService = conversionService;
    }

    @GetMapping
    public ResponseEntity<?> getPage(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                             @RequestParam(value = "size", defaultValue = "20") Integer size,
                                             @RequestParam(value = "archived", defaultValue = "false") Boolean showArchived) {

        Page<Project> pageOfProjects = projectService.getAllPagesAndShowArchivesIs(page, size, showArchived);

        ResolvableType resolvableType = ResolvableType.forClassWithGenerics(
                PageOfTypeDTO.class, ProjectDTO.class
        );

        Object converted = conversionService.convert(
                pageOfProjects, TypeDescriptor.valueOf(PageImpl.class),
                new TypeDescriptor(resolvableType, null, null)
        );

        return new ResponseEntity<>(converted, HttpStatus.OK);

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectCreateDTO projectCreateDTO) {

        projectService.save(projectCreateDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PutMapping(value = "/{uuid}/dt_update/{dt_update}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @PathVariable LocalDateTime dt_update,
                                    @RequestBody ProjectCreateDTO projectCreateDTO) {
        projectService.update(uuid, dt_update, projectCreateDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
