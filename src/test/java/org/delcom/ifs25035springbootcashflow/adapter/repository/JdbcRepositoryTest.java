package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcRepositoryTest {

    private DataSource ds;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource d = new DriverManagerDataSource();
        d.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        d.setUsername("sa");
        d.setPassword("");
        ds = d;
        SchemaInitializer.initialize(ds);
        users = new JdbcUserRepository(ds);
        transactions = new JdbcTransactionRepository(ds);
    }

    @Test
    void schemaInitializerIsIdempotent() {
        SchemaInitializer.initialize(ds);
        SchemaInitializer.initialize(ds);
    }

    @Test
    void userCrud() throws Exception {
        User u = users.save("budi", "h", "Budi", Role.USER);
        assertTrue(u.getId() > 0);
        assertTrue(users.findByUsername("budi").isPresent());
        assertTrue(users.findByUsername("x").isEmpty());
        assertEquals(1, users.findAll().size());
        assertEquals(1, users.findBy(x -> x.getRole() == Role.USER).size());
        assertEquals("Budi", users.findById(u.getId()).getFullName());

        users.updateProfile(u.getId(), "Budi 2", "h2");
        users.updatePhoto(u.getId(), "/uploads/p.png");
        User after = users.findById(u.getId());
        assertEquals("Budi 2", after.getFullName());
        assertEquals("h2", after.getPasswordHash());
        assertEquals("/uploads/p.png", after.getPhotoPath());

        User replacement = new User(u.getId(), "budi", "h3", "B3", Role.USER, null);
        assertEquals("B3", users.update(replacement).getFullName());

        users.delete(u.getId());
        assertTrue(users.findAll().isEmpty());
    }

    @Test
    void userNotFoundPaths() {
        assertThrows(EntityNotFoundException.class, () -> users.findById(99));
        assertThrows(EntityNotFoundException.class, () -> users.delete(99));
        assertThrows(EntityNotFoundException.class, () -> users.updateProfile(99, "a", "b"));
        assertThrows(EntityNotFoundException.class, () -> users.updatePhoto(99, "a"));
        assertThrows(EntityNotFoundException.class,
                () -> users.update(new User(99, "z", "h", "Z", Role.USER, null)));
    }

    @Test
    void transactionCrud() throws Exception {
        User u = users.save("budi", "h", "Budi", Role.USER);
        User other = users.save("ani", "h", "Ani", Role.USER);

        Transaction t = transactions.save(u.getId(), "gaji", 100, TransactionType.INCOME, "/p.png");
        transactions.save(other.getId(), "lain", 5, TransactionType.EXPENSE, null);

        assertEquals(1, transactions.findByUserId(u.getId()).size());
        assertEquals(2, transactions.findAll().size());
        Transaction found = transactions.findById(t.getId());
        assertEquals("gaji", found.getDescription());
        assertEquals(100, found.getAmount());
        assertEquals(TransactionType.INCOME, found.getType());
        assertEquals("/p.png", found.getProofPath());
        assertNotNull(found.getCreatedAt());

        Transaction changed = new Transaction(t.getId(), u.getId(), "baru", 250,
                TransactionType.EXPENSE, null, found.getCreatedAt());
        assertEquals("baru", transactions.update(changed).getDescription());
        assertEquals(250, transactions.findById(t.getId()).getAmount());

        transactions.delete(t.getId());
        assertEquals(1, transactions.findAll().size());
    }

    @Test
    void transactionNotFoundPaths() {
        assertThrows(EntityNotFoundException.class, () -> transactions.findById(99));
        assertThrows(EntityNotFoundException.class, () -> transactions.delete(99));
        assertThrows(EntityNotFoundException.class, () -> transactions.update(
                new Transaction(99, 1, "x", 1, TransactionType.INCOME, null,
                        java.time.LocalDateTime.now())));
    }
}