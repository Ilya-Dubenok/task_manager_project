package org.example.dao.api;

import org.example.dao.entities.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IUserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUuid(UUID uuid);

    List<User> findAll();


}
