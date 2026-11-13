package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class TransactionRepository extends InMemoryRepository<Transaction>
        implements ITransactionRepository {

    @Override
    protected Integer idOf(Transaction t) {
        return t.getId();
    }

    @Override
    public Transaction save(int userId, String description, long amount,
                            TransactionType type, String proofPath) {
        Transaction t = new Transaction(sequence.incrementAndGet(), userId, description,
                amount, type, proofPath, LocalDateTime.now());
        store.put(t.getId(), t);
        return t;
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}