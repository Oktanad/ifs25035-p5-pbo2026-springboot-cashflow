package org.delcom.ifs25035springbootcashflow.adapter.presenter;

import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FinancePresenter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.US);

    /** Contoh: 1000000 menjadi "Rp 1,000,000.00". */
    public String formatMoney(long amount) {
        return "Rp " + String.format(Locale.US, "%,.2f", (double) amount);
    }

    public String typeLabel(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    public String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMAT);
    }

    /** Format: [id] keterangan | Rp 1,000,000.00 | [Pemasukan] */
    public String formatRow(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription()
                + " | " + formatMoney(t.getAmount())
                + " | [" + typeLabel(t.getType()) + "]";
    }

    /** Contoh: "Saldo: Rp 4,975,000.00". */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String successAdded(Transaction t) {
        return "Berhasil menambah: " + formatRow(t);
    }

    public String successDeleted(int id) {
        return "Transaksi [" + id + "] berhasil dihapus";
    }

    public String failure(String message) {
        return "Gagal: " + message;
    }
}