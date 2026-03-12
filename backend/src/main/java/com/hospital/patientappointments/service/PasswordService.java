package com.hospital.patientappointments.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        return encoder.encode(normalize(rawPassword));
    }

    public boolean matches(String rawPassword, String storedPassword) {
        String normalizedRawPassword = normalize(rawPassword);
        if (isLegacyPlaintext(storedPassword)) {
            return normalize(storedPassword).equals(normalizedRawPassword);
        }
        return encoder.matches(normalizedRawPassword, normalize(storedPassword));
    }

    public boolean isLegacyPlaintext(String storedPassword) {
        String normalizedStoredPassword = normalize(storedPassword);
        return !(normalizedStoredPassword.startsWith("$2a$")
            || normalizedStoredPassword.startsWith("$2b$")
            || normalizedStoredPassword.startsWith("$2y$"));
    }

    public boolean shouldUpgrade(String storedPassword) {
        return isLegacyPlaintext(storedPassword);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return value.trim();
    }
}