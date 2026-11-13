package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements IUserRepository {

    private static final RowMapper<User> MAPPER = (rs, n) -> new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            Role.valueOf(rs.getString("role")),
            rs.getString("photo_path"));

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role) VALUES (?,?,?,?)",
                    new String[]{"id"});
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        return new User(id, username, passwordHash, fullName, role, null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = ?", MAPPER, username)
                .stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", MAPPER);
    }

    @Override
    public User findById(Integer id) throws EntityNotFoundException {
        return jdbc.query("SELECT * FROM users WHERE id = ?", MAPPER, id)
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "User dengan ID " + id + " tidak ditemukan"));
    }

    @Override
    public User update(User user) throws EntityNotFoundException {
        findById(user.getId());
        jdbc.update("UPDATE users SET full_name = ?, password_hash = ?, photo_path = ? WHERE id = ?",
                user.getFullName(), user.getPasswordHash(), user.getPhotoPath(), user.getId());
        return user;
    }

    @Override
    public void delete(Integer id) throws EntityNotFoundException {
        findById(id);
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public void updateProfile(int id, String fullName, String passwordHash)
            throws EntityNotFoundException {
        findById(id);
        jdbc.update("UPDATE users SET full_name = ?, password_hash = ? WHERE id = ?",
                fullName, passwordHash, id);
    }

    @Override
    public void updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        findById(id);
        jdbc.update("UPDATE users SET photo_path = ? WHERE id = ?", photoPath, id);
    }
}