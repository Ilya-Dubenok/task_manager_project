package org.example.dao.api;

import org.example.dao.entities.verification.EmailStatus;
import org.example.dao.entities.verification.VerificationInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


public interface IVerificationInfoRepository extends CrudRepository<VerificationInfo, String> {

    VerificationInfo findByMail(String mail);

    @Query(
            value = "DELETE FROM verification_info WHERE " +
                    "EXTRACT(epoch FROM (?1-registered_time))/60 > ?2"
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

    @Query(
            value = "UPDATE verification_info SET " +
                    "email_status = ?1 WHERE mail = ?2"
            , nativeQuery = true
    )
    @Modifying
    @Transactional
    void setEmailStatus(String status, String mail);


    List<VerificationInfo> findByEmailStatusIsAndCountOfAttemptsIsLessThan(EmailStatus status, Integer countOfAttempts);

    @Query(
            value = "UPDATE verification_info SET " +
                    "count_of_attempts = count_of_attempts + 1 WHERE " +
                    "mail = ?1"
            , nativeQuery = true
    )
    @Modifying
    @Transactional
    void increaseCountOfFailedAttempts(String mail);

}
