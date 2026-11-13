package org.delcom.ifs25035springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SortOptionTest {

    private final LocalDateTime now = LocalDateTime.now();

    private Transaction t(int id, long amount, TransactionType type, int minutes) {
        return new Transaction(id, 1, "d" + id, amount, type, null, now.plusMinutes(minutes));
    }

    private List<Integer> ids(SortOption o, List<Transaction> data) {
        List<Transaction> copy = new ArrayList<>(data);
        copy.sort(o.getComparator());
        return copy.stream().map(Transaction::getId).toList();
    }

    @Test
    void sortsByAmountAndType() {
        List<Transaction> data = List.of(
                t(1, 300, TransactionType.EXPENSE, 1),
                t(2, 100, TransactionType.INCOME, 2),
                t(3, 200, TransactionType.INCOME, 3),
                t(4, 400, TransactionType.EXPENSE, 4));

        assertEquals(List.of(2, 3, 1, 4), ids(SortOption.AMOUNT_ASC, data));
        assertEquals(List.of(4, 1, 3, 2), ids(SortOption.AMOUNT_DESC, data));
        assertEquals(List.of(3, 2, 4, 1), ids(SortOption.INCOME_FIRST, data));
        assertEquals(List.of(4, 1, 3, 2), ids(SortOption.EXPENSE_FIRST, data));
    }

    @Test
    void labelsAreSet() {
        for (SortOption o : SortOption.values()) {
            assertNotNull(o.getLabel());
            assertNotNull(o.getComparator());
        }
        assertEquals(4, SortOption.values().length);
    }
}