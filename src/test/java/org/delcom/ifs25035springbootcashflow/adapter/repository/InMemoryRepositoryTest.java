package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    @Test
    void userRepositoryCrud() throws Exception {
        UserRepository repo = new UserRepository();
        User u = repo.save("budi", "h", "Budi", Role.USER);
        assertEquals(1, u.getId());
        assertTrue(repo.findByUsername("budi").isPresent());
        assertTrue(repo.findByUsername("x").isEmpty());
        assertEquals(1, repo.findAll().size());
        assertEquals(1, repo.findBy(x -> x.getRole() == Role.USER).size());

        repo.updateProfile(1, "Budi 2", "h2");
        repo.updatePhoto(1, "/p.png");
        assertEquals("Budi 2", repo.findById(1).getFullName());
        assertEquals("/p.png", repo.findById(1).getPhotoPath());

        User replacement = new User(1, "budi", "h3", "B3", Role.USER, null);
        assertEquals("B3", repo.update(replacement).getFullName());
        assertThrows(EntityNotFoundException.class,
                () -> repo.update(new User(9, "z", "h", "Z", Role.USER, null)));

        repo.delete(1);
        assertTrue(repo.findAll().isEmpty());
        assertThrows(EntityNotFoundException.class, () -> repo.delete(1));
        assertThrows(EntityNotFoundException.class, () -> repo.findById(1));
        assertThrows(EntityNotFoundException.class, () -> repo.updateProfile(1, "a", "b"));
        assertThrows(EntityNotFoundException.class, () -> repo.updatePhoto(1, "a"));
    }

    @Test
    void transactionRepository() throws Exception {
        TransactionRepository repo = new TransactionRepository();
        Transaction t = repo.save(1, "gaji", 100, TransactionType.INCOME, null);
        repo.save(2, "lain", 5, TransactionType.EXPENSE, null);
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(2, repo.findAll().size());
        assertEquals("gaji", repo.findById(t.getId()).getDescription());
        repo.delete(t.getId());
        assertEquals(1, repo.findAll().size());
    }
}