package org.example.service.api;

import org.example.core.dto.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@FeignClient(value = "user-service", url = "http://url.placeholder")
public interface IUserServiceFeignClient {

    @RequestMapping(method = RequestMethod.GET, value = "/user/{uuid}", consumes = "application/json")
    ResponseEntity<UserDTO> getUser(URI baseUrl, @PathVariable("uuid") UUID uuid);

    @RequestMapping(method = RequestMethod.POST, value = "/user", consumes = "application/json")
    ResponseEntity<Set<UserDTO>> getSetOfUsers(URI baseUrl, @RequestBody List<UUID> uuids);


}
