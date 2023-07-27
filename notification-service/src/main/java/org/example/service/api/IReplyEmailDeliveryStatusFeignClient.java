package org.example.service.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.util.Map;

@FeignClient(value = "userservice", url = "http://just.some.place.holder")
public interface IReplyEmailDeliveryStatusFeignClient {

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<?> sendStatusOfDelivery(URI baseUrl, @RequestBody Map<String, Object> body);

}
