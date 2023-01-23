package ru.axbit.jpa;

import ru.axbit.model.AbstractEntity;

import java.util.Collections;
import java.util.List;

public interface Repository<T, ID> {
    List<T> findAll();

    T findById(ID id);

    void delete(T object);

    void deleteById(ID id);

    void update(T object);

    T create(T object);

    Class<T> getClazz();

    default List<AbstractEntity> findAll(String clazz) {
        return Collections.emptyList();
    }

}
