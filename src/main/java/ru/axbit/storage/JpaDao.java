package ru.axbit.storage;

import lombok.Setter;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

@Service("jpa")
public class JpaDao implements InvocationHandler {
    private static final AtomicLong id = new AtomicLong(0);
    @Setter
    private Storage storage;
    @Setter
    private String clazz;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ObjectUtils.isEmpty(args)) {
            args = new Object[]{clazz};
        } else {
            args = ArrayUtils.addFirst(args, clazz);
        }
        try {
            return MethodUtils.invokeMethod(storage, method.getName(), args);
        } catch (Exception e) {
            return MethodUtils.invokeMethod(storage, method.getName(), ArrayUtils.remove(args, 0));
        }
    }
}
