package org.example.service.api;

import org.example.core.dto.PageOfUserDTO;

public interface IUserService {


    PageOfUserDTO getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage);
}
