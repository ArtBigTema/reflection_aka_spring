package ru.axbit.model;

import lombok.NoArgsConstructor;
import ru.axbit.annotation.Entity;
import ru.axbit.utils.Constant;

import javax.annotation.Nonnull;

@Entity("обложка")
public class Cover extends AbstractEntity {
    @Nonnull
    Constant.Colour colour;
}
