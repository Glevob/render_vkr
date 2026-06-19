package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "physical_norms")
public class PhysicalNorm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    // Тип результата: COUNT (кол-во раз) или TIME (сек)
    @Enumerated(EnumType.STRING)
    private ResultType resultType;

    // Возрастная группа
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup; // UNDER_25, OVER_25, ALL

    // Числовое значение результата (уже в приведённом виде)
    private Double value;

    // Баллы по таблице
    private Integer points;

    public enum AgeGroup {
        ALL,
        UNDER_25,
        OVER_25
    }

}
