package org.example.service.api;

import org.example.dao.entities.user.User;

public interface IUserService {

    User findUserInCurrentContext();

}
