package org.example.endpoint.web;

import org.example.core.dto.user.PageOfUserDTO;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserLoginDTO;
import org.example.dao.entities.user.User;
import org.example.service.UserHolder;
import org.example.service.UserServiceImpl;
import org.example.utils.jwt.JwtTokenHandler;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserServlet {

    private final UserServiceImpl service;

    private final ConversionService conversionService;


    private final UserHolder userHolder;

    public UserServlet(UserServiceImpl service, ConversionService conversionService, UserHolder userHolder) {
        this.service = service;
        this.conversionService = conversionService;
        this.userHolder = userHolder;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO userCreateDTO) {


        service.save(userCreateDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);


    }

    @PutMapping(value = "/{uuid}/dt_update/{dt_update}")
    public ResponseEntity<?> updateUser(@PathVariable UUID uuid, @PathVariable LocalDateTime dt_update,
                                        @RequestBody UserCreateDTO userCreateDTO) {
        service.updateUser(uuid, dt_update, userCreateDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<UserDTO> getUserByUuid(@PathVariable UUID uuid) {

        User userById = service.getUserById(uuid);
        UserDTO dto = conversionService.convert(userById, UserDTO.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageOfUserDTO> getPageOfUsers(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                        @RequestParam(value = "size", defaultValue = "20") Integer size) {

        Page<User> pageOfUsers = service.getPageOfUsers(page, size);
        return new ResponseEntity<>(
                conversionService.convert(pageOfUsers, PageOfUserDTO.class),
                HttpStatus.OK
        );
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {

        service.login(userLoginDTO);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    //TODO remove?
    @PostMapping(value = "/login/token")
    public ResponseEntity<?> loginToken(@RequestBody UserLoginDTO userLoginDTO) {

        String token = service.loginAndReceiveToken(userLoginDTO);

        return ResponseEntity.status(HttpStatus.OK).header("Bearer ", token).build();

    }

    @GetMapping(value = "/me")
    public ResponseEntity<UserDTO> getMe() {


        User userById = service.getUserById(UUID.fromString(userHolder.getUser().getUsername()));
        UserDTO dto = conversionService.convert(userById, UserDTO.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);

    }


}
