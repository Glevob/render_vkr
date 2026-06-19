package ru.accouting.student.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhysicalResultFormItem {
    private Long studentId;

    // Выбранные упражнения (по номеру из Прил.10)
    private Integer strengthExerciseNumber;
    private Integer speedExerciseNumber;
    private Integer enduranceExerciseNumber;

    // Результаты
    private Integer strengthResult;
    private Double speedResult;
    private Double enduranceResult;
}
