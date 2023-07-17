package org.example.dao.api;

import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IUserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUuid(UUID uuid);

    List<User> findAll();


    Window<User> findAllByOrderByUuid(Pageable pageable);

    //TODO REFACTOR INTO CONSTANT
    @Query(
            value = "update users set status = 'ACTIVATED' " +
                    "WHERE mail = ?1"
            , nativeQuery = true
    )
    @Modifying
    @Transactional
    int setUserActiveByEmail(String email);




}
