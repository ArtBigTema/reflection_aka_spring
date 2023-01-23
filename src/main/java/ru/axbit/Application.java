package ru.axbit;

import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.annotations.common.reflection.java.generics.TypeUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.axbit.annotation.Commandable;
import ru.axbit.annotation.Entity;
import ru.axbit.annotation.Jpa;
import ru.axbit.service.CrudService;
import ru.axbit.storage.JpaDao;
import ru.axbit.storage.Storage;
import ru.axbit.utils.Commands;
import ru.axbit.utils.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import static ru.axbit.utils.Utils.getService;

@RequiredArgsConstructor
public class Application {
    private final CrudService crudService;

    public static void main(String[] args) throws Exception {
        String packageName = Application.class.getPackageName();
        Reflections reflections = new Reflections(packageName,
                new SubTypesScanner(false), new TypeAnnotationsScanner());

        System.out.println(reflections.getAllTypes()); // печать всего что есть

        Set<Class<?>> serviceClass = reflections.getTypesAnnotatedWith(Service.class);

        Properties prop = new Properties(); // вытаскиваем место хранения
        prop.load(Utils.class.getClassLoader().getResourceAsStream("application.properties"));
        String storage = prop.getProperty("storage");

        List<Object> services = new LinkedList<>();
        for (Class<?> clazz : serviceClass) { // Создаем экземпляры сервисов
            services.add(ConstructorUtils.invokeConstructor(clazz));
        }

        JpaDao dao = getService("jpa", services, JpaDao.class);
        Storage store = getService(storage, services, Storage.class);

        Set<Class<?>> repositoryClass = reflections.getTypesAnnotatedWith(Jpa.class);
        Map<String, Object> repositories = new HashMap<>();
        for (Class<?> repo : repositoryClass) {
            String repository = StringUtils.substringBefore(repo.getSimpleName(), "Repository");
            JpaDao clone = Utils.copy(dao);
            clone.setStorage(store);
            clone.setClazz(repository); // Создаем прокси для всех репозиториев
            repositories.put(repository, Reflection.newProxy(repo, clone));
        }

        for (Object service : services) { // инжектим все бины в сервисы
            Field[] fields = FieldUtils.getFieldsWithAnnotation(service.getClass(), Autowired.class);
            for (Field field : fields) {
                if (TypeUtils.isCollection(field.getType())) {
                    String typeName = field.getGenericType().getTypeName();
                    if (StringUtils.contains(typeName, Repository.class.getSimpleName())) {
                        FieldUtils.writeField(field, service, repositories, true);
                    }
                } else {
                    if (field.getType().equals(Storage.class)) {
                        FieldUtils.writeField(field, service, store, true);
                    }
                }
            }
        }

        System.out.println("all configured");

        // Вытаскиваем все сущности с которыми можем работать
        Map<String, Class<?>> objects = new HashMap<>();
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Entity.class);
        for (Class<?> aClass : typesAnnotatedWith) {
            objects.put(aClass.getAnnotation(Entity.class).value(), aClass);
        }

        // вытаскиваем главный сервис для работы с сущностями
        CrudService crud = getService("crud", services, CrudService.class);
        new Application(crud).startGui(objects);
    }

    private void startGui(Map<String, Class<?>> entities) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // Создаем отображение команд типа Создай объект чего-либо
        List<String> whats = new LinkedList<>(); // список объектов
        Map<Pair<String, String>, Method> methodMap = new HashMap<>();
        for (Method method : crudService.getClass().getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof Commandable c) {
                    whats.add(c.name());
                    methodMap.put(Pair.of(c.value().getTitle(), c.name()), method);
                }
            }
        }

        while (true) { // пока есть консольные команды
            String command = scanner.nextLine();
            String[] microCommand = StringUtils.split(command, " ");

            String action = microCommand[0]; // пусть первое это действие
            String what = microCommand[1]; // второе это объект (или версия или всё)
            // какие-либо аргументы, типа номера (индекса)
            String[] args = ArrayUtils.subarray(microCommand, 2, microCommand.length);

            // корректируем через jaccard
            String correctAction = Utils.similar(action, Commands.Command.values(), Commands.Command::getTitle);
            String correctWhat = Utils.similar(what, whats);

            // определяем целевой метод
            Method method = methodMap.get(Pair.of(correctAction, correctWhat));
            if (Objects.isNull(method)) {
                System.err.println("Метод не найден, попробуйте ещё раз");
                continue;
            }
            if (method.getParameterCount() != args.length) {
                List<String> arg = Lists.newLinkedList();
                for (Parameter parameter : method.getParameters()) {
                    arg.add(parameter.getAnnotation(Name.class).value());
                }
                System.err.println("Необходимы аргументы метода " + arg);
                continue;
            }
            if (args.length > 0) {
                String similar = Utils.similar(args[0], entities.keySet());
                args[0] = entities.get(similar).getSimpleName();
            }

            // выполняем действие с консоли с аргументами
            Object result = method.invoke(crudService, args);
            System.out.println(result); // печатаем результат выполнения
        }

    }
}
