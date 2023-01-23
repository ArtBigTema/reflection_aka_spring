package ru.axbit.jpa;

import ru.axbit.annotation.Jpa;
import ru.axbit.model.Catalog;

@Jpa
public interface CatalogRepository extends Repository<Catalog, Long> {
    @Override
    default Class<Catalog> getClazz() {
        return Catalog.class;
    }
}
