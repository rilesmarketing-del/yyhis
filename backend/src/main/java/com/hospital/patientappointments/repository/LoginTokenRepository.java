package com.hospital.patientappointments.repository;

import com.hospital.patientappointments.model.LoginToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginTokenRepository extends JpaRepository<LoginToken, String> {

    Optional<LoginToken> findByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime threshold);
}