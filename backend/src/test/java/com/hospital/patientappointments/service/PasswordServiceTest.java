package com.hospital.patientappointments.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void encodeReturnsNonPlaintextHash() {
        String encoded = passwordService.encode("patient123");

        assertNotEquals("patient123", encoded);
        assertTrue(encoded.startsWith("$2"));
    }

    @Test
    void matchesAcceptsCorrectPasswordForHash() {
        String encoded = passwordService.encode("doctor123");

        assertTrue(passwordService.matches("doctor123", encoded));
    }

    @Test
    void matchesRejectsWrongPasswordForHash() {
        String encoded = passwordService.encode("admin123");

        assertFalse(passwordService.matches("wrong-password", encoded));
    }

    @Test
    void detectsLegacyPlaintextPasswords() {
        assertTrue(passwordService.isLegacyPlaintext("patient123"));
        assertFalse(passwordService.isLegacyPlaintext("$2a$10$abcdefghijklmnopqrstuuKmbZhSx3x4QwDFi1Cdm42Hcps225y7i"));
    }

    @Test
    void shouldUpgradeLegacyPlaintextButNotBcryptHashes() {
        assertTrue(passwordService.shouldUpgrade("doctor123"));
        assertFalse(passwordService.shouldUpgrade("$2b$10$abcdefghijklmnopqrstuuKmbZhSx3x4QwDFi1Cdm42Hcps225y7i"));
    }
}