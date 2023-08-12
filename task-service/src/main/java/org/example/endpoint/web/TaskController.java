package org.example.endpoint.web;


import org.example.core.dto.task.TaskCreateDTO;
import org.example.service.api.ITaskService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final ITaskService taskService;

    private final ConversionService conversionService;


    public TaskController(ITaskService taskService, ConversionService conversionService) {
        this.taskService = taskService;
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TaskCreateDTO taskCreateDTO) {

        taskService.saveWithUserRoleInContextCheck(taskCreateDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }



}
