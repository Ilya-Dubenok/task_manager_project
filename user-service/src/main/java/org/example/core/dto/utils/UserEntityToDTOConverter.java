package org.example.core.dto.utils;

import org.example.core.dto.PageOfUserDTO;
import org.example.core.dto.UserDTO;
import org.example.dao.entities.user.User;
import org.springframework.data.domain.Window;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserEntityToDTOConverter {

    public static PageOfUserDTO convertWindofOfUsersToPageOfUserDTO(Window<User> window,
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

    public static Integer countNumberOfPagesToReturn(Long totalNumOfReturnedElements, Integer rowsPerPage) {
        long res = totalNumOfReturnedElements / rowsPerPage;
        if ((totalNumOfReturnedElements - res * rowsPerPage) > 0) {
            res++;
        }

        return Math.toIntExact(res);
    }

    public static UserDTO convertUserEntityToUserDto(User user) {
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

    public static List<UserDTO> convertListOfUsersToListOfUserDTOs(List<User> listOfUsers) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (User user : listOfUsers) {
            userDTOList.add(convertUserEntityToUserDto(user));
        }
        return userDTOList;

    }

    public static Long convertLocalDateTimeToLongInMillis(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
