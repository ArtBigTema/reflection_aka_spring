package ru.axbit.utils;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.text.similarity.JaccardDistance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.util.CastUtils;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public class Utils {

    @SneakyThrows
    public static <T> T copy(T src) {
        Class<T> aClass = CastUtils.cast(src.getClass());
        T o = ConstructorUtils.invokeConstructor(aClass);
        copyProperties(src, o);
        return o;
    }

    public static void copyProperties(Object src, Object target, String... ignoredFields) {
        if (Objects.isNull(src)) return;
        BeanWrapper wrappedSrc = new BeanWrapperImpl(src);
        PropertyDescriptor[] propertyDescriptors = wrappedSrc.getPropertyDescriptors();
        Set<String> emptyNames = Sets.newHashSet(ignoredFields);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Object srcValue = null;
            if (emptyNames.contains(propertyDescriptor.getName())) continue;
            try {
                srcValue = wrappedSrc.getPropertyValue(propertyDescriptor.getName());
            } catch (Exception ignored) {
            }
            if (srcValue == null) emptyNames.add(propertyDescriptor.getName());
        }
        String[] result = new String[emptyNames.size()];
        BeanUtils.copyProperties(src, target, emptyNames.toArray(result));
    }

    @SneakyThrows
    public static String readFile(String prop) {
        InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(prop);
        return new String(inputStream.readAllBytes(), Charset.forName("cp1251"));
    }

    /**
     * Предикат для проверки соответствия ошибки ЕГИСЗ ошибке дублирования регистровой записи.
     */
    private BiFunction<String, String, Double> textSimilar = (l, r) -> new JaccardDistance() {
        @Override
        public Double apply(CharSequence left, CharSequence right) {
            return super.apply(StringUtils.lowerCase(left.toString()),
                    StringUtils.lowerCase(right.toString()));
        }
    }.apply(l, r);

    public static String similar(String command, String[] commands) {
        return similar(command, Arrays.asList(commands));
    }

    public static <T> String similar(String command, T[] commands, Function<T, String> converter) {
        return similar(command, Arrays.asList(commands).stream().map(converter).toList());
    }

    public static String similar(String command, Set<String> commands) {
        return similar(command, new ArrayList<>(commands));
    }

    public static String similar(String command, List<String> commands) {
        int index = NumberUtils.INTEGER_MINUS_ONE;
        double max = Double.MAX_VALUE;

        for (int i = 0; i < commands.size(); i++) {
            Double compare = textSimilar.apply(command, commands.get(i));
            if (compare < max) {
                max = compare;
                index = i;
            }
        }
        return commands.get(index);


    }
}
