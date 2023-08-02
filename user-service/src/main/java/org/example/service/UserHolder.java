package org.example.service;



import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserHolder {

    public UserDetails getUser(){
        try {

            return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (ClassCastException e) {

            throw new UsernameNotFoundException("anonymousUserOnly", e);
        }
    }

}
