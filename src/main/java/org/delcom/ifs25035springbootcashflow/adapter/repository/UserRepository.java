package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IUserRepository;

import java.util.Optional;

public class UserRepository extends InMemoryRepository<User> implements IUserRepository {

    @Override
    protected Integer idOf(User user) {
        return user.getId();
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        User user = new User(sequence.incrementAndGet(), username, passwordHash, fullName, role, null);
        store.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return store.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public void updateProfile(int id, String fullName, String passwordHash)
            throws EntityNotFoundException {
        User user = findById(id);
        user.setFullName(fullName);
        user.setPasswordHash(passwordHash);
    }

    @Override
    public void updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        findById(id).setPhotoPath(photoPath);
    }
}