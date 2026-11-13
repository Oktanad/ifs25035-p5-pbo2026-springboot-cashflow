package org.delcom.ifs25035springbootcashflow.usecase;

import org.delcom.ifs25035springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25035springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {

    private FinanceUseCase finance;

    @BeforeEach
    void setUp() {
        finance = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpense() throws Exception {
        Transaction in = finance.addIncome(1, " Gaji ", " 5000 ", "/p.png");
        Transaction out = finance.addExpense(1, "Makan", "1000", null);
        assertEquals("Gaji", in.getDescription());
        assertEquals(5000, in.getAmount());
        assertEquals(TransactionType.INCOME, in.getType());
        assertEquals(TransactionType.EXPENSE, out.getType());
    }

    @Test
    void addValidations() {
        assertThrows(ValidationException.class, () -> finance.addIncome(1, null, "10", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(1, " ", "10", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(1, "a", null, null));
        assertThrows(ValidationException.class, () -> finance.addIncome(1, "a", " ", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(1, "a", "abc", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(1, "a", "0", null));
        assertThrows(ValidationException.class, () -> finance.addIncome(1, "a", "-5", null));
    }

    @Test
    void listAndSearchAreScopedAndCaseInsensitive() throws Exception {
        finance.addIncome(1, "Gaji Bulanan", "100", null);
        finance.addExpense(1, "Makan", "10", null);
        finance.addIncome(2, "Gaji Orang Lain", "999", null);

        assertEquals(2, finance.list(1).size());
        assertEquals(2, finance.search(1, null).size());
        assertEquals(2, finance.search(1, "  ").size());
        List<Transaction> r = finance.search(1, "GAJI");
        assertEquals(1, r.size());
        assertEquals("Gaji Bulanan", r.get(0).getDescription());
        assertTrue(finance.search(1, "zzz").isEmpty());
    }

    @Test
    void sortUsesComparator() throws Exception {
        finance.addIncome(1, "a", "300", null);
        finance.addIncome(1, "b", "100", null);
        List<Transaction> sorted = finance.sort(finance.list(1), SortOption.AMOUNT_ASC);
        assertEquals(100, sorted.get(0).getAmount());
    }

    @Test
    void summaryAndBalance() throws Exception {
        finance.addIncome(1, "a", "5000000", null);
        finance.addExpense(1, "b", "25000", null);
        FinanceUseCase.Summary s = finance.getSummary(1);
        assertEquals(2, s.count());
        assertEquals(5_000_000, s.income());
        assertEquals(25_000, s.expense());
        assertEquals(4_975_000, s.balance());
        assertEquals(4_975_000, finance.getBalance(1));
        assertEquals(0, finance.getBalance(99));
    }

    @Test
    void updateKeepsOrReplacesProof() throws Exception {
        Transaction t = finance.addIncome(1, "a", "100", "/old.png");
        finance.update(1, t.getId(), "baru", "200", null);
        Transaction after = finance.list(1).get(0);
        assertEquals("baru", after.getDescription());
        assertEquals(200, after.getAmount());
        assertEquals("/old.png", after.getProofPath());

        finance.update(1, t.getId(), "baru", "200", "/new.png");
        assertEquals("/new.png", finance.list(1).get(0).getProofPath());
    }

    @Test
    void updateAndDeleteOwnershipAndIds() throws Exception {
        Transaction t = finance.addIncome(1, "a", "100", null);
        assertThrows(ValidationException.class, () -> finance.update(1, 0, "a", "1", null));
        assertThrows(ValidationException.class, () -> finance.update(2, t.getId(), "a", "1", null));
        assertThrows(EntityNotFoundException.class, () -> finance.update(1, 99, "a", "1", null));
        assertThrows(ValidationException.class, () -> finance.delete(1, -1));
        assertThrows(ValidationException.class, () -> finance.delete(2, t.getId()));

        finance.delete(1, t.getId());
        assertTrue(finance.list(1).isEmpty());
        assertThrows(EntityNotFoundException.class, () -> finance.delete(1, t.getId()));
    }
}