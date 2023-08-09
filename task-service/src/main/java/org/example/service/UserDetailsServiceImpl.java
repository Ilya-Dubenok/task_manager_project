package org.example.service;

import org.example.core.dto.user.UserDTO;
import org.example.service.api.IUserServiceRequester;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

public class UserDetailsServiceImpl implements UserDetailsService {

    private IUserServiceRequester userServiceRequester;

    public UserDetailsServiceImpl(IUserServiceRequester userServiceRequester) {
        this.userServiceRequester = userServiceRequester;
    }

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {


        UUID parsedUUID;
        try {

            parsedUUID = UUID.fromString(uuid);

        } catch (Exception e) {
            throw new UsernameNotFoundException("passed uuid is malformed");
        }

        UserDTO userDTO = userServiceRequester.getUser(parsedUUID);

        if (userDTO == null) {
            throw new UsernameNotFoundException("No user found for this id");
        }

        return User.builder()
                .username(String.valueOf(userDTO.getUuid()))
                .password("no_password")
                .roles(userDTO.getRole().toString())
                .build();


    }
}
