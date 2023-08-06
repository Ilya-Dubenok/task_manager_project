package org.example.dao.api;

import org.example.dao.entities.verification.VerificationInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.UUID;


public interface IVerificationInfoRepository extends CrudRepository<VerificationInfo, UUID> {

    VerificationInfo findByMail(String mail);

    @Query(
            value = "DELETE FROM verification_info WHERE " +
                    "EXTRACT(epoch FROM (?1-registered_time))/60 > ?2"
            , nativeQuery = true
    )
    @Modifying
    int cleanOldCodes(LocalDateTime currentMoment, Integer diffInMinutes);

    @Query(
            value = "DELETE FROM verification_info WHERE " +
                    "mail = ?1"
            , nativeQuery = true
    )
    @Modifying
    int cleanUsedCode(String mail);

    @Query(
            value = "UPDATE verification_info SET " +
                    "email_status = ?1 WHERE mail = ?2"
            , nativeQuery = true
    )
    @Modifying
    void setEmailStatus(String status, String mail);



}
