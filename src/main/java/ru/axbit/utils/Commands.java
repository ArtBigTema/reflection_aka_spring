package ru.axbit.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class Commands {
    @Getter
    @RequiredArgsConstructor
    public enum Command {
        PRINT("печать"),
        CREATE("создать"),
        UPDATE("обновить"),
        DELETE("удалить");

        private final String title;
    }
}
