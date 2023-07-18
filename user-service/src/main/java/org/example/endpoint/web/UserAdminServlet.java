package org.example.endpoint.web;

import jakarta.servlet.http.HttpServletResponse;
import org.example.core.dto.UserCreateDTO;
import org.example.core.exception.StructuredException;
import org.example.core.exception.StructuredExceptionDTO;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserAdminServlet {

    IUserService service;

    public UserAdminServlet(IUserService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody UserCreateDTO userCreateDTO) {


        service.saveFromApiSource(userCreateDTO);
        //TODO WRITE GENERAL METHOD TO HANDLE EXCEPTIONS AND GIVE EXCEPTION_DTO


        return new ResponseEntity<>(HttpStatus.CREATED);


    }

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<User> getUserByUuid(@PathVariable String uuid) {

        User userById = service.getUserById(UUID.fromString(uuid));
        return new ResponseEntity<User>(userById,HttpStatus.OK);
    }


    //TODO CHANGE AS REQUIRED
    @ExceptionHandler({Exception.class})
    public ResponseEntity<?> printStructuredException(HttpServletResponse response, StructuredException e) throws IOException {
        StructuredExceptionDTO structuredExceptionDTO = new StructuredExceptionDTO(e);
        return new ResponseEntity<>(structuredExceptionDTO, HttpStatus.BAD_REQUEST);
    }

}
