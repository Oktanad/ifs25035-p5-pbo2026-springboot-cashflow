package org.delcom.ifs25035springbootcashflow.usecase;

import org.delcom.ifs25035springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25035springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

public class FinanceUseCase {

    /** Ringkasan untuk kartu: total, pemasukan, pengeluaran, saldo. */
    public record Summary(int count, long income, long expense) {
        public long balance() {
            return income - expense;
        }
    }

    private final ITransactionRepository repo;

    public FinanceUseCase(ITransactionRepository repo) {
        this.repo = repo;
    }

    public Transaction addIncome(int userId, String description, String amount, String proofPath)
            throws DomainException {
        return add(userId, description, amount, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(int userId, String description, String amount, String proofPath)
            throws DomainException {
        return add(userId, description, amount, TransactionType.EXPENSE, proofPath);
    }

    private Transaction add(int userId, String description, String amount,
                            TransactionType type, String proofPath) throws DomainException {
        String desc = validDescription(description);
        long value = parseAmount(amount);
        return repo.save(userId, desc, value, type, proofPath);
    }

    public List<Transaction> list(int userId) {
        return repo.findByUserId(userId);
    }

    /** Filter keterangan, case-insensitive, hanya data milik userId. */
    public List<Transaction> search(int userId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return list(userId);
        }
        String k = keyword.trim().toLowerCase();
        return list(userId).stream()
                .filter(t -> t.getDescription().toLowerCase().contains(k))
                .toList();
    }

    public List<Transaction> sort(List<Transaction> data, SortOption option) {
        return data.stream().sorted(option.getComparator()).toList();
    }

    public Transaction update(int userId, int id, String description, String amount, String proofPath)
            throws DomainException {
        Transaction t = owned(userId, id);
        t.setDescription(validDescription(description));
        t.setAmount(parseAmount(amount));
        if (proofPath != null) {
            t.setProofPath(proofPath);
        }
        return repo.update(t);
    }

    public void delete(int userId, int id) throws DomainException {
        owned(userId, id);
        repo.delete(id);
    }

    public Summary getSummary(int userId) {
        List<Transaction> all = list(userId);
        long income = all.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToLong(Transaction::getAmount).sum();
        long expense = all.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount).sum();
        return new Summary(all.size(), income, expense);
    }

    public long getBalance(int userId) {
        return getSummary(userId).balance();
    }

    private Transaction owned(int userId, int id) throws DomainException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
        Transaction t = repo.findById(id);
        if (t.getUserId() != userId) {
            throw new ValidationException("Transaksi bukan milik Anda");
        }
        return t;
    }

    private String validDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private long parseAmount(String amount) throws ValidationException {
        if (amount == null || amount.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long value;
        try {
            value = Long.parseLong(amount.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (value <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return value;
    }
}