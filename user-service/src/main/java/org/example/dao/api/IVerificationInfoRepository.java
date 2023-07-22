package org.example.dao.api;

import org.example.dao.entities.verification.VerificationInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


public interface IVerificationInfoRepository extends CrudRepository<VerificationInfo, String> {

    VerificationInfo findByMail(String mail);

    @Query(
            value = "DELETE FROM verification_info WHERE " +
                    "EXTRACT(epoch FROM (?1-expiration_time))/60 > ?2"
            , nativeQuery = true
    )
    @Modifying
    @Transactional
    int cleanOldCodes(LocalDateTime currentMoment, Integer diffInMinutes);

    @Query(
            value = "DELETE FROM verification_info WHERE " +
                    "mail = ?1"
            , nativeQuery = true
    )
    @Modifying
    @Transactional
    int cleanUsedCode(String mail);

}
