package com.hospital.patientappointments.repository;

import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.model.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameAndEnabledTrue(String username);

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    List<UserAccount> findAllByRoleAndEnabledTrueOrderByUsernameAsc(UserRole role);
}