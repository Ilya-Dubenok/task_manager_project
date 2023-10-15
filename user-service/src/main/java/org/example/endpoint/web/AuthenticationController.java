package org.example.endpoint.web;


import org.example.core.dto.user.UserLoginDTO;
import org.example.core.dto.user.UserRegistrationDTO;
import org.example.service.api.IAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class AuthenticationController {


    private IAuthenticationService service;

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);


    public AuthenticationController(IAuthenticationService service) {
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

    @PostMapping(value = "/login")
    public ResponseEntity<?> loginToken(@RequestBody UserLoginDTO userLoginDTO) {

        LOG.warn("FIRST-WARNING");
        LOG.info("SECOND-INFO");

        try {
            throw new RuntimeException("My custom exception is thrown");
        } catch (RuntimeException e) {

            LOG.error("THIRD-EXCEPTION",e);

        }

        String token = service.loginAndReceiveToken(userLoginDTO);

        return new ResponseEntity<>(token, HttpStatus.OK);

    }


}
