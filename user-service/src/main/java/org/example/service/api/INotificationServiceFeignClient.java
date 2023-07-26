package org.example.service.api;

import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

@FeignClient(value = "notificationservice", url = "${app.network.audit_service.host}${app.network.audit_service.appendix}")
public interface INotificationServiceFeignClient {

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<?> sendEmail(URI baseUrl, @RequestBody JSONObject object);

}