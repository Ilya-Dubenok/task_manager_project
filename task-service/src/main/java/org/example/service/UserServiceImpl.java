package org.example.service;

import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.example.service.api.IUserServiceRequester;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

    private IUserRepository userRepository;

    private IUserServiceRequester userServiceRequester;

    public UserServiceImpl(IUserRepository userRepository, IUserServiceRequester userServiceRequester) {
        this.userRepository = userRepository;
        this.userServiceRequester = userServiceRequester;
    }


    @Override
    public User findUserInCurrentContext() {
        return null;
    }
}
