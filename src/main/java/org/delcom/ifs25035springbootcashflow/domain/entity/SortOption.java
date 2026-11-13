package org.delcom.ifs25035springbootcashflow.domain.entity;

import java.util.Comparator;

public enum SortOption {

    AMOUNT_ASC("Jumlah naik",
            Comparator.comparingLong(Transaction::getAmount)),

    AMOUNT_DESC("Jumlah turun",
            Comparator.comparingLong(Transaction::getAmount).reversed()),

    INCOME_FIRST("Pemasukan dulu",
            Comparator.comparingInt((Transaction t) -> t.getType() == TransactionType.INCOME ? 0 : 1)
                    .thenComparing(Transaction::getCreatedAt, Comparator.reverseOrder())),

    EXPENSE_FIRST("Pengeluaran dulu",
            Comparator.comparingInt((Transaction t) -> t.getType() == TransactionType.EXPENSE ? 0 : 1)
                    .thenComparing(Transaction::getCreatedAt, Comparator.reverseOrder()));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String getLabel() {
        return label;
    }

    public Comparator<Transaction> getComparator() {
        return comparator;
    }
}