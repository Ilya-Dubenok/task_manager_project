package org.example.service;

import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private IUserService userService;

    public UserDetailsServiceImpl(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        UUID parsedUUID;
        try {
            parsedUUID = UUID.fromString(uuid);
        } catch (Exception e) {
            throw new UsernameNotFoundException("passed uuid is malformed");
        }
        User userById = userService.getUserById(
                parsedUUID
        );

        if (userById == null) {
            throw new UsernameNotFoundException("No user found for this id");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(userById.getUuid()))
                .password(userById.getPassword())
                .roles(userById.getRole().toString())
                .build();

    }
}
