package org.example.service;

import org.example.config.property.ApplicationProperties;
import org.example.core.dto.user.UserDTO;
import org.example.core.exception.GeneralException;
import org.example.service.api.IUserServiceFeignClient;
import org.example.service.api.IUserServiceRequester;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.UUID;

@Service
public class UserServiceRequesterImpl implements IUserServiceRequester {


    private final String URL_PREFIX = "http://";

    private final URI USER_SERVICE_INTERNAL_URL;

    private final IUserServiceFeignClient userServiceFeignClient;

    public UserServiceRequesterImpl(IUserServiceFeignClient userServiceFeignClient, ApplicationProperties applicationProperties) {
        this.userServiceFeignClient = userServiceFeignClient;
        USER_SERVICE_INTERNAL_URL = formUserServiceUrl(applicationProperties);

    }

    private URI formUserServiceUrl(ApplicationProperties properties) {
        final URI USER_SERVICE_URL;
        ApplicationProperties.NetworkProp.UserService userService = properties.getNetwork().getUserService();

        if (userService.getAddress() == null || userService.getAddress().isBlank()) {

            USER_SERVICE_URL = URI.create(

                    URL_PREFIX + userService.getHost() + userService.getInternalAppendix());
        } else {

            USER_SERVICE_URL = URI.create(
                    URL_PREFIX + userService.getAddress() + userService.getInternalAppendix()
            );
        }
        return USER_SERVICE_URL;

    }


    @Override
    public UserDTO getUser(UUID uuid) {
        try {
            ResponseEntity<UserDTO> response = userServiceFeignClient.getUser(USER_SERVICE_INTERNAL_URL, uuid);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }

            return null;

        } catch (Exception e) {

            throw new GeneralException("При обработке запроса произошла ошибка", e);

        }
    }
}
