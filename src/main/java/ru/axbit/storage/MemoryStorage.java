package ru.axbit.storage;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ReflectionUtils;
import ru.axbit.model.AbstractEntity;
import ru.axbit.utils.Utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service("memory")
public class MemoryStorage implements Storage {
    private static final AtomicLong id = new AtomicLong(0);
    private LinkedMultiValueMap<String, AbstractEntity> storage;

    public MemoryStorage() {
        storage = new LinkedMultiValueMap<>();
    }

    @Override
    public List<AbstractEntity> findAll() {
    throw new RuntimeException("");
    }

    @Override
    public AbstractEntity findById(Long s) {
        Collection<List<AbstractEntity>> values = storage.values();
        for (List<AbstractEntity> value : values) { // fixme if not found thr
            for (AbstractEntity abstractEntity : value) {
                if (abstractEntity.getId().equals(s)) {
                    return abstractEntity;
                }
            }
        }
        throw new RuntimeException("not found");
    }

    @Override
    public void delete(AbstractEntity object) {
        deleteById(object.getId());
    }

    @Override
    public void deleteById(Long s) {
        Collection<List<AbstractEntity>> values = storage.values();
        for (List<AbstractEntity> value : values) { // fixme if not found thr
            value.removeIf(e -> e.getId().equals(s));
        }
    }

    @Override
    public void update(AbstractEntity object) {
        String simpleName = object.getClass().getSimpleName();
        List<AbstractEntity> abstractEntities = findAll(simpleName);
        boolean exist = false;
        for (AbstractEntity abstractEntity : abstractEntities) {
            if (abstractEntity.getId().equals(object.getId())) {
                Utils.copyProperties(object, abstractEntity);
                exist = true;
            }
        }

        if (!exist) {
            throw new RuntimeException("Сущность не найдена");
        }
    }

    @Override
    public AbstractEntity create(AbstractEntity object) {
        Preconditions.checkState(Objects.nonNull(object), "У новых объектов id выставляем сами");
        String simpleName = object.getClass().getSimpleName();
        object.setId(id.addAndGet(NumberUtils.LONG_ONE));
        storage.add(simpleName, object);
        return object;
    }

    @Override
    public List<AbstractEntity> findAll(String clazz) {
        return storage.getOrDefault(clazz, new LinkedList<>());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return ReflectionUtils.invokeMethod(method, this, args);
    }
}
