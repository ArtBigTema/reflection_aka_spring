package ru.axbit.model;

import lombok.Data;
import ru.axbit.annotation.Entity;

import javax.annotation.Nonnull;
import java.util.List;

@Data
@Entity("справочник")
public class Catalog extends AbstractEntity {
    @Nonnull
    String title; // семейная, рабочая

    List<Element> elements;
}
