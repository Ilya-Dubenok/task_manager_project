package org.example.endpoint.web;

import org.example.core.dto.UserCreateDTO;
import org.example.core.dto.UserDTO;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserAdminServlet {

    private IUserService service;

    private ConversionService conversionService;

    public UserAdminServlet(IUserService service, ConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO userCreateDTO) {


        service.saveFromApiSource(userCreateDTO);
        //TODO WRITE GENERAL METHOD TO HANDLE EXCEPTIONS AND GIVE EXCEPTION_DTO


        return new ResponseEntity<>(HttpStatus.CREATED);


    }

    @PutMapping(value = "/{uuid}/dt_update/{dt_update}")
    public ResponseEntity<?> updateUser(@PathVariable UUID uuid, @PathVariable LocalDateTime dt_update,
                                           @RequestBody UserCreateDTO userCreateDTO) {
        service.updateUser(uuid, dt_update, userCreateDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<?> getUserByUuid(@PathVariable UUID uuid) {

        User userById = service.getUserById(uuid);
        UserDTO dto = conversionService.convert(userById, UserDTO.class);
        return new ResponseEntity<>(dto,HttpStatus.OK);
    }



}
