package ru.axbit.model;

import lombok.Data;
import ru.axbit.annotation.Entity;

import javax.annotation.Nonnull;

@Data
@Entity("Элемент")
public class Element extends AbstractEntity {
    String phone;
    @Nonnull
    String fio;
}
