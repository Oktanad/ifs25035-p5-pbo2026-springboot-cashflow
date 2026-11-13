package org.delcom.ifs25035springbootcashflow.usecase;

import org.delcom.ifs25035springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class AuthUseCase {

    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws DomainException {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username tidak boleh kosong");
        }
        if (password == null || password.length() < 4) {
            throw new ValidationException("Password minimal 4 karakter");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (users.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return users.save(username.trim(), PasswordHasher.hash(password),
                fullName.trim(), Role.USER);
    }

    public User login(String username, String password) throws DomainException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        Optional<User> found = users.findByUsername(username.trim());
        if (found.isEmpty() || !PasswordHasher.verify(password, found.get().getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return found.get();
    }

    /** Membuat admin dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(username, PasswordHasher.hash(password), "Administrator", Role.ADMIN);
        }
    }
}