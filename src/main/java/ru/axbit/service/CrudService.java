package ru.axbit.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jeasy.random.EasyRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Service;
import ru.axbit.annotation.Commandable;
import ru.axbit.jpa.Repository;
import ru.axbit.model.AbstractEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.axbit.utils.Commands.Command.CREATE;
import static ru.axbit.utils.Commands.Command.DELETE;
import static ru.axbit.utils.Commands.Command.PRINT;
import static ru.axbit.utils.Utils.readFile;

@Service("crud")
public class CrudService {
    @Autowired
    private Map<String, ? extends Repository<?, ?>> repositoryMap;


    @SneakyThrows
    @Commandable(value = PRINT, name = "все")
    public <T extends AbstractEntity> List<T> findAll(@Name("Имя класса") String className) {
        Class<?> clazz = Class.forName("ru.axbit.model.".concat(className));
        return findAll(clazz);
    }

    @SneakyThrows
    @Commandable(value = PRINT, name = "объект")
    public <T extends AbstractEntity> AbstractEntity findById(@Name("Имя класса") String className, @Name("ид") String id) {
        return getRepository(className).findById(NumberUtils.createLong(id));
    }

    @SneakyThrows
    @Commandable(value = CREATE, name = "объект")
    public <T extends AbstractEntity> T create(@Name("Имя класса") String className/*, @Name("объект") T object*/) {
        Class<?> clazz = Class.forName("ru.axbit.model.".concat(className));
        Object o = new EasyRandom().nextObject(clazz);
        return CastUtils.cast(getRepository(className).create(CastUtils.cast(o)));
    }

    @SneakyThrows
    @Commandable(value = DELETE, name = "объект")
    public <T extends AbstractEntity> void delete(@Name("Имя класса") String className, @Name("Ид") String id) {
        Repository<AbstractEntity, ?> repository = getRepository(className);
        repository.delete(findById(className, id));
    }

    public <T extends AbstractEntity> List<T> findAll(Class<?> clazz) {
        Repository<?, ?> repository = getRepository(clazz);
        return CastUtils.cast(repository.findAll());
    }

    @SneakyThrows
    private <T extends AbstractEntity> Repository<T, Long> getRepository(String className) {
        Class<?> clazz = Class.forName("ru.axbit.model.".concat(className));
        return getRepository(clazz);
    }

    private <T extends AbstractEntity> Repository<T, Long> getRepository(Class<?> clazz) {
        Repository<?, ?> repository = repositoryMap.get(clazz.getSimpleName());
        return CastUtils.cast(Objects.requireNonNull(repository, "Not found"));
    }

    @SneakyThrows
    @Commandable(value = PRINT, name = "версия")
    public Object version() {
        String cp1251 = readFile("git.properties");
        String[] split = StringUtils.split(cp1251, "\n");
        return Arrays.stream(split)
                .filter(s -> StringUtils.contains(s, '='))
                .map(s -> StringUtils.split(s, "="))
                .filter(s -> s.length > 1)
                .collect(Collectors.toMap(s -> s[0], s -> s[1].trim()));
    }
}
