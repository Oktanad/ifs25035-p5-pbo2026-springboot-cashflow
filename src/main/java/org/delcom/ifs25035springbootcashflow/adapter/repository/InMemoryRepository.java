package org.delcom.ifs25035springbootcashflow.adapter.repository;

import org.delcom.ifs25035springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25035springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class InMemoryRepository<T> implements IRepository<T, Integer> {

    protected final Map<Integer, T> store = new LinkedHashMap<>();
    protected final AtomicInteger sequence = new AtomicInteger();

    protected abstract Integer idOf(T entity);

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public T findById(Integer id) throws EntityNotFoundException {
        T entity = store.get(id);
        if (entity == null) {
            throw new EntityNotFoundException("Data dengan ID " + id + " tidak ditemukan");
        }
        return entity;
    }

    @Override
    public T update(T entity) throws EntityNotFoundException {
        findById(idOf(entity));
        store.put(idOf(entity), entity);
        return entity;
    }

    @Override
    public void delete(Integer id) throws EntityNotFoundException {
        findById(id);
        store.remove(id);
    }
}