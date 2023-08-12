package org.example.endpoint.web;


import org.example.core.dto.project.ProjectCreateDTO;
import org.example.service.api.IProjectService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final IProjectService projectService;

    private final ConversionService conversionService;


    public ProjectController(IProjectService projectService, ConversionService conversionService) {
        this.projectService = projectService;
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectCreateDTO projectCreateDTO) {

        projectService.save(projectCreateDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }
}
