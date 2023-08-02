package org.example.dao.api;

import org.example.dao.entities.user.User;
import org.example.dao.entities.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    Page<User> findAllByOrderByUuid(Pageable pageable);

    User findByMail(String email);

    User findByMailAndStatusEquals(String email, UserStatus status);



    @Query(
            value = "update users set status = 'ACTIVATED' " +
                    "WHERE mail = ?1 AND status= 'WAITING_ACTIVATION'"
            , nativeQuery = true
    )
    @Modifying
    @Transactional
    int setUserActiveByEmail(String email);




}
