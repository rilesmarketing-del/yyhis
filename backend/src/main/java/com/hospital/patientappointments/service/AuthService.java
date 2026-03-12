package com.hospital.patientappointments.service;

import com.hospital.patientappointments.dto.CurrentUserResponse;
import com.hospital.patientappointments.dto.LoginResponse;
import com.hospital.patientappointments.dto.RegisterPatientRequest;
import com.hospital.patientappointments.dto.RegisterPatientResponse;
import com.hospital.patientappointments.model.AuthenticatedUser;
import com.hospital.patientappointments.model.LoginToken;
import com.hospital.patientappointments.model.UserAccount;
import com.hospital.patientappointments.repository.LoginTokenRepository;
import com.hospital.patientappointments.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final LoginTokenRepository loginTokenRepository;
    private final AccountProvisioningService accountProvisioningService;
    private final PasswordService passwordService;
    private final long tokenHours;

    public AuthService(UserAccountRepository userAccountRepository,
                       LoginTokenRepository loginTokenRepository,
                       AccountProvisioningService accountProvisioningService,
                       PasswordService passwordService,
                       @Value("${demo.auth.token-hours:24}") long tokenHours) {
        this.userAccountRepository = userAccountRepository;
        this.loginTokenRepository = loginTokenRepository;
        this.accountProvisioningService = accountProvisioningService;
        this.passwordService = passwordService;
        this.tokenHours = tokenHours;
    }

    @Transactional
    public LoginResponse login(String username, String password) {
        UserAccount account = userAccountRepository.findByUsernameAndEnabledTrue(username == null ? "" : username.trim())
            .orElseThrow(() -> new AuthException("Invalid username or password"));
        if (!matchesPassword(password, account.getPassword())) {
            throw new AuthException("Invalid username or password");
        }
        if (passwordService.shouldUpgrade(account.getPassword())) {
            account.setPassword(passwordService.encode(password));
            userAccountRepository.save(account);
        }

        loginTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        String token = UUID.randomUUID().toString().replace("-", "");
        LoginToken loginToken = new LoginToken(
            token,
            account.getId(),
            account.getUsername(),
            account.getRole(),
            account.getDisplayName(),
            account.getPatientId(),
            LocalDateTime.now().plusHours(tokenHours)
        );
        loginTokenRepository.save(loginToken);
        return new LoginResponse(token, account.getRole().getCode(), account.getUsername(), account.getDisplayName(), account.getPatientId());
    }

    @Transactional
    public RegisterPatientResponse registerPatient(RegisterPatientRequest request) {
        UserAccount account = accountProvisioningService.registerPatient(request);
        return new RegisterPatientResponse(account.getUsername(), account.getDisplayName(), account.getRole().getCode(), account.getPatientId());
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser resolveToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        LoginToken loginToken = loginTokenRepository.findByToken(token.trim()).orElse(null);
        if (loginToken == null || loginToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return new AuthenticatedUser(loginToken.getUsername(), loginToken.getRole(), loginToken.getDisplayName(), loginToken.getPatientId());
    }

    @Transactional
    public void logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }
        loginTokenRepository.deleteById(token.trim());
    }

    public CurrentUserResponse toCurrentUserResponse(AuthenticatedUser user) {
        return new CurrentUserResponse(user.getUsername(), user.getRole().getCode(), user.getDisplayName(), user.getPatientId());
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        try {
            return passwordService.matches(rawPassword, storedPassword);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}