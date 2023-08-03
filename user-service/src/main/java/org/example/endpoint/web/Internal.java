package org.example.endpoint.web;


import org.example.service.api.IAuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class Internal {

    IAuthenticationService service;

    public Internal(IAuthenticationService service) {
        this.service = service;
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

}
