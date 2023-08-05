package org.example.endpoint.web;


import org.example.core.dto.user.UserDTO;
import org.example.dao.entities.user.User;
import org.example.service.api.IAuthenticationService;
import org.example.service.api.IUserService;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal")
public class InternalRequestsServlet {

    private IAuthenticationService service;

    private final IUserService userService;

    private final ConversionService conversionService;



    public InternalRequestsServlet(IAuthenticationService service, IUserService userService, ConversionService conversionService) {
        this.service = service;
        this.userService = userService;
        this.conversionService = conversionService;
    }

    @PostMapping("/notification")
    public ResponseEntity<?> setEmailIsDeliveredOrNotToUser(@RequestBody Map<String, Object> body) {

        if (body.get("mail") != null && body.get("status") != null) {
            String mail = (String) body.get("mail");
            Boolean status = (Boolean) body.get("status");
            service.setEmailDeliveryStatus(mail, status);

        }
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @GetMapping(value = "/user/{uuid}")
    public ResponseEntity<UserDTO> getByUuid(@PathVariable UUID uuid) {

        User userById = userService.getByUUID(uuid);
        UserDTO dto = conversionService.convert(userById, UserDTO.class);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping(value = "/user")
    public ResponseEntity<List<UserDTO>> getByListOfUuids(@RequestBody List<UUID> uuidList) {

        List<User> userList = userService.getList(uuidList);
        List<UserDTO> res = userList.stream()
                .map(x -> conversionService.convert(x, UserDTO.class))
                .toList();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }



}