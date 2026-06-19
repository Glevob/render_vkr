package ru.accouting.student.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "exercise")
@Getter
@Setter
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Номер упражнения
    @Column(name = "number", nullable = false)
    private Integer number;

    // Вид упражнения (название)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // Категория упражнения: Сила, Быстрота, Выносливость
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ExerciseCategory category;

    // Тип результата: количество раз, время
    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false, length = 50)
    private ResultType resultType;

    // Возрастная группа, для которой применяется упражнение
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    private AgeGroup ageGroup;

    // Связь Упражнения с Нормами
    // Удаление Упражнения каскадно удалит все Нормы
    @OneToMany(mappedBy = "exercise",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    private java.util.List<PhysicalNorm> norms;

    public enum ExerciseCategory {
        STRENGTH,   // Сила
        SPEED,      // Быстрота
        ENDURANCE   // Выносливость
    }

    public enum ResultType {
        REPS,       // количество раз
        TIME        // время
    }

    public enum AgeGroup {
        ALL,        // Все
        OVER_25     // Старше 25
    }
}