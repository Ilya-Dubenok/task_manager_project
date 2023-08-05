package org.example.endpoint.web;

import org.example.core.dto.user.PageOfUserDTO;
import org.example.core.dto.user.UserCreateDTO;
import org.example.core.dto.user.UserDTO;
import org.example.core.dto.user.UserLoginDTO;
import org.example.dao.entities.user.User;
import org.example.service.UserHolder;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final IUserService userService;

    private final ConversionService conversionService;


    private final UserHolder userHolder;

    public UserController(IUserService userService, ConversionService conversionService, UserHolder userHolder) {
        this.userService = userService;
        this.conversionService = conversionService;
        this.userHolder = userHolder;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserCreateDTO userCreateDTO) {


        userService.save(userCreateDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);


    }

    @PutMapping(value = "/{uuid}/dt_update/{dt_update}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @PathVariable LocalDateTime dt_update,
                                        @RequestBody UserCreateDTO userCreateDTO) {
        userService.update(uuid, dt_update, userCreateDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{uuid}")
    public ResponseEntity<UserDTO> getByUuid(@PathVariable UUID uuid) {

        User userById = userService.getByUUID(uuid);
        UserDTO dto = conversionService.convert(userById, UserDTO.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<PageOfUserDTO> getPageOfUsers(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                        @RequestParam(value = "size", defaultValue = "20") Integer size) {

        Page<User> pageOfUsers = userService.getPage(page, size);
        return new ResponseEntity<>(
                conversionService.convert(pageOfUsers, PageOfUserDTO.class),
                HttpStatus.OK
        );
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {

        userService.login(userLoginDTO);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    //TODO remove?
    @PostMapping(value = "/login/token")
    public ResponseEntity<?> loginToken(@RequestBody UserLoginDTO userLoginDTO) {

        String token = userService.loginAndReceiveToken(userLoginDTO);

        return ResponseEntity.status(HttpStatus.OK).header("Bearer", token).build();

    }

    @GetMapping(value = "/me")
    public ResponseEntity<UserDTO> getMe() {


        User userById = userService.getByUUID(UUID.fromString(userHolder.getUser().getUsername()));
        UserDTO dto = conversionService.convert(userById, UserDTO.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);

    }


}
