package org.example.dao.api;

import org.example.dao.entities.audit.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface IAuditRepository extends CrudRepository<Audit,UUID> {

    Optional<Audit> findByUuid(UUID uuid);

    Page<Audit> findAllByOrderByUuid(Pageable pageable);

}
