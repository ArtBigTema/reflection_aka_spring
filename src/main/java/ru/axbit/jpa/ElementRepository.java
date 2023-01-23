package ru.axbit.jpa;

import ru.axbit.annotation.Jpa;
import ru.axbit.model.Element;

@Jpa
public interface ElementRepository extends Repository<Element, Long> {
    @Override
    default Class<Element> getClazz() {
        return Element.class;
    }
}
