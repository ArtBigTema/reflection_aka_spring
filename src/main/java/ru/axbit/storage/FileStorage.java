package ru.axbit.storage;

import org.springframework.stereotype.Service;
import ru.axbit.model.AbstractEntity;

import java.util.List;

@Service("file")
public class FileStorage implements Storage {// todo

    @Override
    public AbstractEntity findById(Long s) {
        return null;
    }

    @Override
    public List<AbstractEntity> findAll() {
        throw new RuntimeException("");
    }

    @Override
    public void delete(AbstractEntity object) {

    }

    @Override
    public void deleteById(Long s) {

    }

    @Override
    public void update(AbstractEntity object) {

    }

    @Override
    public AbstractEntity create(AbstractEntity object) {
        return object;
    }

    @Override
    public List<AbstractEntity> findAll(String clazz) {
        return null;
    }
}
