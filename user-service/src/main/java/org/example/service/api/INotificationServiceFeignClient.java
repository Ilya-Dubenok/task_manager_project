package org.example.service.api;

import org.example.core.dto.email.EmailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

@FeignClient(value = "notificationservice", url = "http://url.placeholder")
public interface INotificationServiceFeignClient {

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<?> sendEmail(URI baseUrl, @RequestBody EmailDTO object);

}