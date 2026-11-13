package org.delcom.ifs25035springbootcashflow.usecase;

import org.delcom.ifs25035springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25035springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserUseCaseTest {

    private UserRepository repo;
    private UserUseCase users;
    private User u;

    @BeforeEach
    void setUp() {
        repo = new UserRepository();
        users = new UserUseCase(repo);
        u = repo.save("budi", PasswordHasher.hash("1234"), "Budi", Role.USER);
    }

    @Test
    void listAndGet() throws Exception {
        assertEquals(1, users.list().size());
        assertEquals("budi", users.get(u.getId()).getUsername());
        assertThrows(EntityNotFoundException.class, () -> users.get(99));
    }

    @Test
    void updateProfileWithoutPasswordKeepsHash() throws Exception {
        String old = u.getPasswordHash();
        users.updateProfile(u.getId(), " Budi Baru ", null);
        assertEquals("Budi Baru", users.get(u.getId()).getFullName());
        assertEquals(old, users.get(u.getId()).getPasswordHash());

        users.updateProfile(u.getId(), "Budi", "  ");
        assertEquals(old, users.get(u.getId()).getPasswordHash());
    }

    @Test
    void updateProfileChangesPassword() throws Exception {
        users.updateProfile(u.getId(), "Budi", "abcd");
        assertTrue(PasswordHasher.verify("abcd", users.get(u.getId()).getPasswordHash()));
    }

    @Test
    void updateProfileValidations() {
        assertThrows(ValidationException.class, () -> users.updateProfile(u.getId(), null, "abcd"));
        assertThrows(ValidationException.class, () -> users.updateProfile(u.getId(), " ", "abcd"));
        assertThrows(ValidationException.class, () -> users.updateProfile(u.getId(), "Budi", "123"));
        assertThrows(EntityNotFoundException.class, () -> users.updateProfile(99, "Budi", null));
    }

    @Test
    void updatePhoto() throws Exception {
        users.updatePhoto(u.getId(), "/uploads/a.png");
        assertEquals("/uploads/a.png", users.get(u.getId()).getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> users.updatePhoto(99, "/x.png"));
    }
}