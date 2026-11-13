package org.delcom.ifs25035springbootcashflow.usecase;

import org.delcom.ifs25035springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

public class UserUseCase {

    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    public List<User> list() {
        return users.findAll();
    }

    public User get(int id) throws EntityNotFoundException {
        return users.findById(id);
    }

    /** newPassword kosong berarti password tidak diubah. */
    public void updateProfile(int id, String fullName, String newPassword) throws DomainException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama tidak boleh kosong");
        }
        User user = users.findById(id);
        String hash = user.getPasswordHash();
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 4) {
                throw new ValidationException("Password minimal 4 karakter");
            }
            hash = PasswordHasher.hash(newPassword);
        }
        users.updateProfile(id, fullName.trim(), hash);
    }

    public void updatePhoto(int id, String photoPath) throws DomainException {
        users.updatePhoto(id, photoPath);
    }
}