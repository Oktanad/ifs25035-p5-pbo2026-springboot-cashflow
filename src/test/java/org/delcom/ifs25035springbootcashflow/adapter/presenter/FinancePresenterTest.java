package org.delcom.ifs25035springbootcashflow.adapter.presenter;

import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter p = new FinancePresenter();

    @Test
    void formatsMoney() {
        assertEquals("Rp 1,000,000.00", p.formatMoney(1_000_000));
        assertEquals("Rp 0.00", p.formatMoney(0));
    }

    @Test
    void formatsLabelsRowsAndMessages() {
        Transaction t = new Transaction(5, 1, "Gaji", 1_000_000, TransactionType.INCOME,
                null, LocalDateTime.of(2026, 1, 5, 9, 30));
        assertEquals("Pemasukan", p.typeLabel(TransactionType.INCOME));
        assertEquals("Pengeluaran", p.typeLabel(TransactionType.EXPENSE));
        assertEquals("05 Jan 2026 09:30", p.formatDate(t.getCreatedAt()));
        assertEquals("[5] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.formatRow(t));
        assertEquals("Saldo: Rp 4,975,000.00", p.formatBalance(4_975_000));
        assertEquals("Berhasil menambah: [5] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.successAdded(t));
        assertEquals("Transaksi [5] berhasil dihapus", p.successDeleted(5));
        assertEquals("Gagal: x", p.failure("x"));
    }
}