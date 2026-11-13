package org.delcom.ifs25035springbootcashflow.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashIsDeterministicSha256Hex() {
        String h = PasswordHasher.hash("abc");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", h);
        assertEquals(h, PasswordHasher.hash("abc"));
    }

    @Test
    void verifyMatchesAndRejects() {
        String h = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.verify("rahasia", h));
        assertFalse(PasswordHasher.verify("salah", h));
    }

    @Test
    void unknownAlgorithmIsWrapped() {
        assertThrows(IllegalStateException.class, () -> PasswordHasher.digest("NOPE", "x"));
    }
}