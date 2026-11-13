package org.delcom.ifs25035springbootcashflow.domain.repository;

import org.delcom.ifs25035springbootcashflow.domain.entity.Role;
import org.delcom.ifs25035springbootcashflow.domain.entity.User;
import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;

import java.util.Optional;

public interface IUserRepository extends IRepository<User, Integer> {

    User save(String username, String passwordHash, String fullName, Role role);

    Optional<User> findByUsername(String username);

    void updateProfile(int id, String fullName, String passwordHash) throws EntityNotFoundException;

    void updatePhoto(int id, String photoPath) throws EntityNotFoundException;
}