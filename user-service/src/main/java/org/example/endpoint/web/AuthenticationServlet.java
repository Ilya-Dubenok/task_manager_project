package org.example.endpoint.web;


import org.example.core.dto.user.UserRegistrationDTO;
import org.example.service.api.IAuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class AuthenticationServlet {


    IAuthenticationService service;

    public AuthenticationServlet(IAuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO userRegistrationDTO) {

        service.registerUser(userRegistrationDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @GetMapping("/verification")
    public ResponseEntity<?> verifyUserWithCode(@RequestParam("code") Integer code, @RequestParam("mail") String mail) {

        service.verifyUserWithCode(code, mail);
        return new ResponseEntity<>(HttpStatus.OK);

    }


}
