package org.example.service;

import feign.FeignException;
import org.example.config.properties.ApplicationProperties;
import org.example.core.dto.user.UserDTO;
import org.example.core.exception.GeneralException;
import org.example.service.api.IUserServiceFeignClient;
import org.example.service.api.IUserServiceRequester;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceFeignRequesterImpl implements IUserServiceRequester {

    private final String URL_PREFIX = "http://";

    private final URI USER_SERVICE_INTERNAL_URL;

    private final IUserServiceFeignClient userServiceFeignRequester;

    public UserServiceFeignRequesterImpl(IUserServiceFeignClient userServiceFeignRequester, ApplicationProperties applicationProperties) {
        this.userServiceFeignRequester = userServiceFeignRequester;
        USER_SERVICE_INTERNAL_URL = formUserServiceUrl(applicationProperties);
    }

    @Override
    public UserDTO getUser(UUID uuid) {

        try {

            ResponseEntity<UserDTO> response = userServiceFeignRequester.getUser(USER_SERVICE_INTERNAL_URL, uuid);

            HttpStatusCode statusCode = response.getStatusCode();
            if (statusCode == HttpStatus.OK) {

                return response.getBody();

            } else if (statusCode == HttpStatus.BAD_REQUEST) {

                return null;

            } else {

                throw new GeneralException("При обработке запроса произошла ошибка");
            }

        } catch (FeignException.FeignClientException.BadRequest badRequest) {

            return null;

        } catch (Exception e) {

            throw new GeneralException("При обработке запроса произошла ошибка", e);

        }
    }

    @Override
    public Set<UserDTO> getSetOfUserDTOs(List<UUID> uuids) {

        try {

            ResponseEntity<Set<UserDTO>> response = userServiceFeignRequester.getSetOfUsers(USER_SERVICE_INTERNAL_URL, uuids);

            if (response.getStatusCode() == HttpStatus.OK) {

                return response.getBody();

            } else {

                throw new GeneralException("При обработке запроса произошла неизвестная ошибка");

            }

        } catch (Exception e) {

            throw new GeneralException("При обработке запроса произошла ошибка", e);

        }
    }

    private URI formUserServiceUrl(ApplicationProperties applicationProperties) {

        final URI USER_SERVICE_URL;

        ApplicationProperties.NetworkProp.UserService userService = applicationProperties.getNetwork().getUserService();

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
}
