package org.example.service;

import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserDTO;
import org.example.dao.api.IUserRepository;
import org.example.dao.entities.user.User;
import org.example.service.api.IUserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl implements IUserService {


    private IUserRepository userRepository;

    public UserServiceImpl(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PageOfUserDTO getPageOfUsers(Integer currentRequestedPage, Integer rowsPerPage) {

        Window<User> userWindow = userRepository.findAllByOrderByUuid(PageRequest.of(currentRequestedPage, rowsPerPage));
        Long count = userRepository.count();
        PageOfUserDTO res = convertWindOfUsersToPageOfUserDTO(
                userWindow, count, currentRequestedPage, rowsPerPage
        );

        return res;

    }


    private PageOfUserDTO convertWindOfUsersToPageOfUserDTO(Window<User> window,
                                                            Long totalNumOfReturnedElements,
                                                            Integer currentRequestedPage,
                                                            Integer rowsPerPage
    ) {
        PageOfUserDTO res = new PageOfUserDTO();
        res.setNumber(currentRequestedPage);
        res.setSize(rowsPerPage);
        Integer totalPages = totalNumOfReturnedElements == 0 ? 0 : countNumberOfPagesToReturn(totalNumOfReturnedElements, rowsPerPage);
        res.setTotalPages(totalPages);
        res.setTotalElements(totalNumOfReturnedElements);
        res.setFirst(currentRequestedPage == 0);
        int sizeOfCurrentWindow = window.size();
        res.setNumberOfElements(sizeOfCurrentWindow);
        res.setLast(window.isLast());
        List<UserDTO> content;
        if (sizeOfCurrentWindow != 0) {
            content = convertListOfUsersToListOfUserDTOs(window.getContent());
        } else {
            content = new ArrayList<>();
        }
        res.setContent(content);
        return res;
    }

    private Integer countNumberOfPagesToReturn(Long totalNumOfReturnedElements, Integer rowsPerPage) {
        long res = totalNumOfReturnedElements / rowsPerPage;
        if ((totalNumOfReturnedElements - res * rowsPerPage) > 0) {
            res++;
        }

        return Math.toIntExact(res);
    }

    private UserDTO convertUserEntityToUserDto(User user) {
        UserDTO res = new UserDTO();
        res.setUuid(user.getUuid());
        res.setDtCreate(convertLocalDateTimeToLongInMillis(user.getDtCreate()));
        res.setDtUpdate(convertLocalDateTimeToLongInMillis(user.getDtUpdate()));
        res.setMail(user.getMail());
        res.setFio(user.getFio());
        res.setRole(user.getRole());
        res.setStatus(user.getStatus());
        return res;

    }

    private List<UserDTO> convertListOfUsersToListOfUserDTOs(List<User> listOfUsers) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : listOfUsers) {
            userDTOList.add(convertUserEntityToUserDto(user));
        }
        return userDTOList;

    }

    private Long convertLocalDateTimeToLongInMillis(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
