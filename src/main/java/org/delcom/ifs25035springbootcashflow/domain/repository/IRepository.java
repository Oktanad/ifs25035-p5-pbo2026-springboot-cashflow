package org.delcom.ifs25035springbootcashflow.domain.repository;

import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;

import java.util.List;
import java.util.function.Predicate;

public interface IRepository<T, ID> {

    List<T> findAll();

    T findById(ID id) throws EntityNotFoundException;

    T update(T entity) throws EntityNotFoundException;

    void delete(ID id) throws EntityNotFoundException;

    default List<T> findBy(Predicate<T> predicate) {
        return findAll().stream().filter(predicate).toList();
    }
}