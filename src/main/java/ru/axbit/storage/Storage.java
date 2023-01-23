package ru.axbit.storage;

import org.springframework.util.ReflectionUtils;
import ru.axbit.jpa.Repository;
import ru.axbit.model.AbstractEntity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public interface Storage extends InvocationHandler, Repository<AbstractEntity, Long> {
    @Override
    default Class<AbstractEntity> getClazz() {
        throw new RuntimeException();
    }

    List<AbstractEntity> findAll(String clazz);

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return ReflectionUtils.invokeMethod(method, proxy, args);
    }
}
