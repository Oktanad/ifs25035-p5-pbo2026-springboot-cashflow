package org.delcom.ifs25035springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void userAccessors() {
        User u = new User(1, "budi", "h", "Budi", Role.USER, null);
        u.setId(2);
        u.setPasswordHash("h2");
        u.setFullName("Budi S");
        u.setPhotoPath("/uploads/a.png");
        assertEquals(2, u.getId());
        assertEquals("budi", u.getUsername());
        assertEquals("h2", u.getPasswordHash());
        assertEquals("Budi S", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertEquals("/uploads/a.png", u.getPhotoPath());
    }

    @Test
    void transactionAccessors() {
        LocalDateTime now = LocalDateTime.now();
        Transaction t = new Transaction(1, 7, "gaji", 100, TransactionType.INCOME, null, now);
        t.setId(3);
        t.setDescription("bonus");
        t.setAmount(250);
        t.setProofPath("/uploads/b.png");
        assertEquals(3, t.getId());
        assertEquals(7, t.getUserId());
        assertEquals("bonus", t.getDescription());
        assertEquals(250, t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals("/uploads/b.png", t.getProofPath());
        assertEquals(now, t.getCreatedAt());
    }

    @Test
    void enumsResolve() {
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(2, Role.values().length);
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
        assertEquals(2, TransactionType.values().length);
    }
}