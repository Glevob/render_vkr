package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "physical_sum_scale")
public class PhysicalSumScale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Сумма баллов по трём упражнениям
    private Integer sumPoints;

    // Возрастная группа
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup; // UNDER_25, OVER_25

    // Результат по 100‑балльной шкале
    private Integer scaledResult;
}
