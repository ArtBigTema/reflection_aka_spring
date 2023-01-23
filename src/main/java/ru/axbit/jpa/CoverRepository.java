package ru.axbit.jpa;

import ru.axbit.annotation.Jpa;
import ru.axbit.model.Cover;

@Jpa
public interface CoverRepository extends Repository<Cover, Long> {
    @Override
    default Class<Cover> getClazz() {
        return Cover.class;
    }
}
