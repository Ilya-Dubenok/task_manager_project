package org.example.dao.api;

import org.example.dao.entities.audit.Audit;
import org.example.core.dto.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAuditRepository extends CrudRepository<Audit,UUID> {

    List<Audit> findAll();

    Optional<Audit> findByUuid(UUID uuid);

    Page<Audit> findAllByOrderByUuid(Pageable pageable);

    List<Audit> findByUserRole(UserRole role);


}
