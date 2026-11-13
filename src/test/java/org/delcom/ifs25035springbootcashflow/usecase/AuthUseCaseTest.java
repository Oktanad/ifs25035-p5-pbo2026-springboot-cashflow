package org.delcom.ifs25035springbootcashflow.usecase;

import org.delcom.ifs25035springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthUseCaseTest {

    private UserRepository repo;
    private AuthUseCase auth;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        auth = new AuthUseCase(repo);
    }

    @Test
    void registerSuccessHashesPassword() throws Exception {
        User u = auth.register(" budi ", "1234", " Budi ");
        assertEquals("budi", u.getUsername());
        assertEquals("Budi", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertNotEquals("1234", u.getPasswordHash());
    }

    @Test
    void registerValidations() throws Exception {
        assertThrows(ValidationException.class, () -> auth.register(null, "1234", "A"));
        assertThrows(ValidationException.class, () -> auth.register(" ", "1234", "A"));
        assertThrows(ValidationException.class, () -> auth.register("a", null, "A"));
        assertThrows(ValidationException.class, () -> auth.register("a", "123", "A"));
        assertThrows(ValidationException.class, () -> auth.register("a", "1234", null));
        assertThrows(ValidationException.class, () -> auth.register("a", "1234", " "));
        auth.register("a", "1234", "A");
        assertThrows(ValidationException.class, () -> auth.register("a", "1234", "A"));
    }

    @Test
    void loginSuccessAndFailures() throws Exception {
        auth.register("budi", "1234", "Budi");
        assertEquals("budi", auth.login(" budi ", "1234").getUsername());

        assertThrows(ValidationException.class, () -> auth.login(null, "1234"));
        assertThrows(ValidationException.class, () -> auth.login(" ", "1234"));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        assertThrows(ValidationException.class, () -> auth.login("budi", ""));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "1234"));
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));
    }

    @Test
    void ensureRootAdminIsIdempotent() {
        auth.ensureRootAdmin("admin", "admin");
        auth.ensureRootAdmin("admin", "admin");
        assertEquals(1, repo.findAll().size());
        assertEquals(Role.ADMIN, repo.findByUsername("admin").orElseThrow().getRole());
    }
}