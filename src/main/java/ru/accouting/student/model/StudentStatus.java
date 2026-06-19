package ru.accouting.student.model;

import lombok.Getter;

@Getter
public enum StudentStatus {
    APPLIED("Подал заявление"),
    PRELIMINARY_PASSED("Прошел предварительный отбор"),
    CONTEST_PASSED("Прошел конкурсный отбор"),
    NOT_PASSED("Не прошел"),
    REJECTED("Отказ"),
    CANDIDATE("Кандидат"),
    CADET("Курсант"),
    GRADUATION("Выпуск"),
    RESERVE("Запас");

    private final String displayName;

    StudentStatus(String displayName) {
        this.displayName = displayName;
    }
}
