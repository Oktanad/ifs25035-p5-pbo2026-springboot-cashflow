package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25035springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class JdbcTransactionRepository implements ITransactionRepository {

    private static final RowMapper<Transaction> MAPPER = (rs, n) -> new Transaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("description"),
            rs.getLong("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getString("proof_path"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Transaction save(int userId, String description, long amount,
                            TransactionType type, String proofPath) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                            + "VALUES (?,?,?,?,?,?)",
                    new String[]{"id"});
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, proofPath);
            ps.setTimestamp(6, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        return new Transaction(id, userId, description, amount, type, proofPath, now);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbc.query(
                "SELECT * FROM transactions WHERE user_id = ? ORDER BY created_at DESC, id DESC",
                MAPPER, userId);
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query("SELECT * FROM transactions ORDER BY id", MAPPER);
    }

    @Override
    public Transaction findById(Integer id) throws EntityNotFoundException {
        return jdbc.query("SELECT * FROM transactions WHERE id = ?", MAPPER, id)
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaksi dengan ID " + id + " tidak ditemukan"));
    }

    @Override
    public Transaction update(Transaction t) throws EntityNotFoundException {
        findById(t.getId());
        jdbc.update("UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?",
                t.getDescription(), t.getAmount(), t.getType().name(), t.getProofPath(), t.getId());
        return t;
    }

    @Override
    public void delete(Integer id) throws EntityNotFoundException {
        findById(id);
        jdbc.update("DELETE FROM transactions WHERE id = ?", id);
    }
}